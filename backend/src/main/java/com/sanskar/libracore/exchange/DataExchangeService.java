package com.sanskar.libracore.exchange;

import com.sanskar.libracore.audit.AuditService;
import com.sanskar.libracore.catalog.CatalogModels.CreateBookRequest;
import com.sanskar.libracore.catalog.CatalogService;
import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.exchange.DataExchangeModels.ImportResult;
import com.sanskar.libracore.member.MemberModels.CreateMemberRequest;
import com.sanskar.libracore.member.MemberService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DataExchangeService {
    private static final int MAX_EXPORT_ROWS = 10_000;

    private final JdbcClient jdbc;
    private final CatalogService catalogService;
    private final MemberService memberService;
    private final AuditService auditService;
    private final Validator validator;

    public DataExchangeService(
            JdbcClient jdbc,
            CatalogService catalogService,
            MemberService memberService,
            AuditService auditService,
            Validator validator
    ) {
        this.jdbc = jdbc;
        this.catalogService = catalogService;
        this.memberService = memberService;
        this.auditService = auditService;
        this.validator = validator;
    }

    public String exportBooks() {
        List<BookExportRow> rows = jdbc.sql("""
                        SELECT b.title, b.subtitle, b.isbn13, b.description, b.language_code,
                               b.publication_year, b.edition_label, p.name AS publisher_name,
                               COALESCE((
                                   SELECT string_agg(a.display_name, '|' ORDER BY ba.contribution_order)
                                   FROM book_author ba
                                   JOIN author a ON a.id = ba.author_id
                                   WHERE ba.book_id = b.id
                               ), '') AS authors,
                               COALESCE((
                                   SELECT string_agg(c.name, '|' ORDER BY c.name)
                                   FROM book_category bc
                                   JOIN category c ON c.id = bc.category_id
                                   WHERE bc.book_id = b.id
                               ), '') AS categories
                        FROM book b
                        LEFT JOIN publisher p ON p.id = b.publisher_id
                        ORDER BY LOWER(b.title), b.id
                        LIMIT :limit
                        """)
                .param("limit", MAX_EXPORT_ROWS + 1)
                .query((rs, rowNum) -> new BookExportRow(
                        rs.getString("title"),
                        rs.getString("subtitle"),
                        rs.getString("isbn13"),
                        rs.getString("description"),
                        rs.getString("language_code"),
                        rs.getObject("publication_year", Integer.class),
                        rs.getString("edition_label"),
                        rs.getString("publisher_name"),
                        rs.getString("authors"),
                        rs.getString("categories")
                ))
                .list();
        ensureExportBound(rows.size());

        StringBuilder csv = new StringBuilder();
        csv.append(CsvCodec.row(List.of(
                "title", "subtitle", "isbn13", "description", "languageCode",
                "publicationYear", "editionLabel", "publisherName", "authors", "categories"
        )));
        for (BookExportRow row : rows) {
            csv.append(CsvCodec.row(List.of(
                    nullToEmpty(row.title()), nullToEmpty(row.subtitle()), nullToEmpty(row.isbn13()),
                    nullToEmpty(row.description()), nullToEmpty(row.languageCode()),
                    row.publicationYear() == null ? "" : row.publicationYear().toString(),
                    nullToEmpty(row.editionLabel()), nullToEmpty(row.publisherName()),
                    nullToEmpty(row.authors()), nullToEmpty(row.categories())
            )));
        }
        return csv.toString();
    }

    public String exportMembers() {
        List<MemberExportRow> rows = jdbc.sql("""
                        SELECT b.code AS branch_code, m.library_card_number, m.first_name, m.last_name,
                               m.email, m.phone, m.status, m.joined_at, m.expires_at, m.notes
                        FROM member m
                        JOIN branch b ON b.id = m.home_branch_id
                        ORDER BY LOWER(m.last_name), LOWER(m.first_name), m.id
                        LIMIT :limit
                        """)
                .param("limit", MAX_EXPORT_ROWS + 1)
                .query((rs, rowNum) -> new MemberExportRow(
                        rs.getString("branch_code"),
                        rs.getString("library_card_number"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("status"),
                        rs.getObject("joined_at", OffsetDateTime.class),
                        rs.getObject("expires_at", OffsetDateTime.class),
                        rs.getString("notes")
                ))
                .list();
        ensureExportBound(rows.size());

        StringBuilder csv = new StringBuilder();
        csv.append(CsvCodec.row(List.of(
                "homeBranchCode", "libraryCardNumber", "firstName", "lastName", "email",
                "phone", "status", "joinedAt", "expiresAt", "notes"
        )));
        for (MemberExportRow row : rows) {
            csv.append(CsvCodec.row(List.of(
                    nullToEmpty(row.branchCode()), nullToEmpty(row.libraryCardNumber()),
                    nullToEmpty(row.firstName()), nullToEmpty(row.lastName()), nullToEmpty(row.email()),
                    nullToEmpty(row.phone()), nullToEmpty(row.status()),
                    row.joinedAt() == null ? "" : row.joinedAt().toString(),
                    row.expiresAt() == null ? "" : row.expiresAt().toString(),
                    nullToEmpty(row.notes())
            )));
        }
        return csv.toString();
    }

    @Transactional
    public ImportResult importBooks(String csv, UUID actorUserId) {
        if (csv == null) {
            throw ApiException.badRequest("csv_missing", "CSV content is required.");
        }
        return importBooks(new StringReader(csv), actorUserId);
    }

    @Transactional
    public ImportResult importBooks(Reader csv, UUID actorUserId) {
        ImportState state = new ImportState();
        CsvCodec.forEachRow(csv, (csvRow, row) -> {
            if (state.headers == null) {
                state.headers = headers(row);
                requireHeaders(state.headers, "title");
                return;
            }
            if (row.stream().allMatch(String::isBlank)) {
                return;
            }

            String title = value(row, state.headers, "title");
            Integer publicationYear = parseIntegerNullable(
                    value(row, state.headers, "publicationyear"),
                    csvRow,
                    "publicationYear"
            );
            String languageCode = value(row, state.headers, "languagecode");
            if (languageCode.isBlank()) {
                languageCode = "en";
            }

            CreateBookRequest request = new CreateBookRequest(
                    title,
                    blankToNull(value(row, state.headers, "subtitle")),
                    blankToNull(value(row, state.headers, "isbn13")),
                    blankToNull(value(row, state.headers, "description")),
                    languageCode,
                    publicationYear,
                    blankToNull(value(row, state.headers, "editionlabel")),
                    blankToNull(value(row, state.headers, "publishername")),
                    splitPipe(value(row, state.headers, "authors")),
                    splitPipe(value(row, state.headers, "categories"))
            );
            validateRequest(request, csvRow);
            try {
                catalogService.createBook(request, actorUserId);
            } catch (ApiException exception) {
                throw rowError(csvRow, exception.getMessage());
            }
            state.imported++;
        });

        requireHeaderRow(state);
        auditService.success(actorUserId, "CSV_IMPORT_BOOKS", "BOOK", null, null,
                "{\"rows\":" + state.imported + "}");
        return new ImportResult("books", state.imported, List.copyOf(state.warnings));
    }

    @Transactional
    public ImportResult importMembers(String csv, UUID actorUserId) {
        if (csv == null) {
            throw ApiException.badRequest("csv_missing", "CSV content is required.");
        }
        return importMembers(new StringReader(csv), actorUserId);
    }

    @Transactional
    public ImportResult importMembers(Reader csv, UUID actorUserId) {
        ImportState state = new ImportState();
        CsvCodec.forEachRow(csv, (csvRow, row) -> {
            if (state.headers == null) {
                state.headers = headers(row);
                requireHeaders(state.headers, "homebranchcode", "librarycardnumber", "firstname", "lastname", "email");
                return;
            }
            if (row.stream().allMatch(String::isBlank)) {
                return;
            }

            String branchCode = value(row, state.headers, "homebranchcode").trim().toUpperCase(Locale.ROOT);
            UUID branchId = jdbc.sql("SELECT id FROM branch WHERE code = :code AND active = TRUE")
                    .param("code", branchCode)
                    .query(UUID.class)
                    .optional()
                    .orElseThrow(() -> rowError(csvRow, "homeBranchCode does not match an active branch"));

            OffsetDateTime expiresAt = parseDateTimeNullable(
                    value(row, state.headers, "expiresat"),
                    csvRow,
                    "expiresAt"
            );
            CreateMemberRequest request = new CreateMemberRequest(
                    branchId,
                    value(row, state.headers, "librarycardnumber"),
                    value(row, state.headers, "firstname"),
                    value(row, state.headers, "lastname"),
                    value(row, state.headers, "email"),
                    blankToNull(value(row, state.headers, "phone")),
                    expiresAt,
                    blankToNull(value(row, state.headers, "notes")),
                    null
            );
            validateRequest(request, csvRow);
            try {
                memberService.createMember(request, actorUserId);
            } catch (ApiException exception) {
                throw rowError(csvRow, exception.getMessage());
            }
            String requestedStatus = value(row, state.headers, "status").trim().toUpperCase(Locale.ROOT);
            if (!requestedStatus.isBlank() && !"ACTIVE".equals(requestedStatus)) {
                if (!List.of("SUSPENDED", "CLOSED").contains(requestedStatus)) {
                    throw rowError(csvRow, "status must be ACTIVE, SUSPENDED, or CLOSED");
                }
                state.warnings.add("Row " + csvRow + ": imported as ACTIVE; change status through the member lifecycle API after review.");
            }
            state.imported++;
        });

        requireHeaderRow(state);
        auditService.success(actorUserId, "CSV_IMPORT_MEMBERS", "MEMBER", null, null,
                "{\"rows\":" + state.imported + "}");
        return new ImportResult("members", state.imported, List.copyOf(state.warnings));
    }

    private static void requireHeaderRow(ImportState state) {
        if (state.headers == null) {
            throw ApiException.badRequest("csv_empty", "CSV must include a header row.");
        }
    }

    private <T> void validateRequest(T request, int row) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return;
        }
        String details = violations.stream()
                .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .findFirst()
                .orElse("contains invalid values");
        throw rowError(row, details);
    }

    private static Map<String, Integer> headers(List<String> headerRow) {
        Map<String, Integer> headers = new LinkedHashMap<>();
        for (int index = 0; index < headerRow.size(); index++) {
            String key = normalizeHeader(headerRow.get(index));
            if (key.isBlank()) {
                continue;
            }
            if (headers.putIfAbsent(key, index) != null) {
                throw ApiException.badRequest("csv_duplicate_header", "CSV contains a duplicate header: " + key);
            }
        }
        return headers;
    }

    private static String normalizeHeader(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
    }

    private static void requireHeaders(Map<String, Integer> headers, String... required) {
        for (String header : required) {
            if (!headers.containsKey(header)) {
                throw ApiException.badRequest("csv_missing_header", "CSV is missing required header: " + header);
            }
        }
    }

    private static String value(List<String> row, Map<String, Integer> headers, String header) {
        Integer index = headers.get(header);
        if (index == null || index >= row.size()) {
            return "";
        }
        return row.get(index).trim();
    }

    private static List<String> splitPipe(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("\\|", -1))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .toList();
    }

    private static Integer parseIntegerNullable(String value, int row, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            throw rowError(row, field + " must be an integer");
        }
    }

    private static OffsetDateTime parseDateTimeNullable(String value, int row, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.trim());
        } catch (RuntimeException exception) {
            throw rowError(row, field + " must be an ISO-8601 timestamp with an offset");
        }
    }

    private static ApiException rowError(int row, String message) {
        return ApiException.badRequest("csv_row_invalid", "CSV row " + row + ": " + message);
    }

    private static void ensureExportBound(int size) {
        if (size > MAX_EXPORT_ROWS) {
            throw ApiException.badRequest(
                    "export_too_large",
                    "This export exceeds 10,000 rows; use a database backup or narrower operational export."
            );
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static final class ImportState {
        private Map<String, Integer> headers;
        private int imported;
        private final List<String> warnings = new ArrayList<>();
    }

    private record BookExportRow(
            String title,
            String subtitle,
            String isbn13,
            String description,
            String languageCode,
            Integer publicationYear,
            String editionLabel,
            String publisherName,
            String authors,
            String categories
    ) {
    }

    private record MemberExportRow(
            String branchCode,
            String libraryCardNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            String status,
            OffsetDateTime joinedAt,
            OffsetDateTime expiresAt,
            String notes
    ) {
    }
}
