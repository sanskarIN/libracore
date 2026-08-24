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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.sql.ResultSet;
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
    private static final int EXPORT_FETCH_SIZE = 250;

    private static final String BOOK_EXPORT_SQL = """
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
            """;

    private static final String MEMBER_EXPORT_SQL = """
            SELECT b.code AS branch_code, m.library_card_number, m.first_name, m.last_name,
                   m.email, m.phone, m.status, m.joined_at, m.expires_at, m.notes
            FROM member m
            JOIN branch b ON b.id = m.home_branch_id
            ORDER BY LOWER(m.last_name), LOWER(m.first_name), m.id
            """;

    private final JdbcClient jdbc;
    private final JdbcTemplate jdbcTemplate;
    private final CatalogService catalogService;
    private final MemberService memberService;
    private final AuditService auditService;
    private final Validator validator;

    public DataExchangeService(
            JdbcClient jdbc,
            JdbcTemplate jdbcTemplate,
            CatalogService catalogService,
            MemberService memberService,
            AuditService auditService,
            Validator validator
    ) {
        this.jdbc = jdbc;
        this.jdbcTemplate = jdbcTemplate;
        this.catalogService = catalogService;
        this.memberService = memberService;
        this.auditService = auditService;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public String exportBooks() {
        StringWriter csv = new StringWriter();
        try {
            writeBooksCsv(csv);
        } catch (IOException exception) {
            throw new IllegalStateException("String-backed CSV export failed unexpectedly.", exception);
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public void writeBooksCsv(Writer writer) throws IOException {
        ensureBooksExportBound();
        writer.write(CsvCodec.row(List.of(
                "title", "subtitle", "isbn13", "description", "languageCode",
                "publicationYear", "editionLabel", "publisherName", "authors", "categories"
        )));

        streamQuery(BOOK_EXPORT_SQL, resultSet -> writer.write(CsvCodec.row(List.of(
                nullToEmpty(resultSet.getString("title")),
                nullToEmpty(resultSet.getString("subtitle")),
                nullToEmpty(resultSet.getString("isbn13")),
                nullToEmpty(resultSet.getString("description")),
                nullToEmpty(resultSet.getString("language_code")),
                integerToString(resultSet.getObject("publication_year", Integer.class)),
                nullToEmpty(resultSet.getString("edition_label")),
                nullToEmpty(resultSet.getString("publisher_name")),
                nullToEmpty(resultSet.getString("authors")),
                nullToEmpty(resultSet.getString("categories"))
        ))));
    }

    @Transactional(readOnly = true)
    public String exportMembers() {
        StringWriter csv = new StringWriter();
        try {
            writeMembersCsv(csv);
        } catch (IOException exception) {
            throw new IllegalStateException("String-backed CSV export failed unexpectedly.", exception);
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public void writeMembersCsv(Writer writer) throws IOException {
        ensureMembersExportBound();
        writer.write(CsvCodec.row(List.of(
                "homeBranchCode", "libraryCardNumber", "firstName", "lastName", "email",
                "phone", "status", "joinedAt", "expiresAt", "notes"
        )));

        streamQuery(MEMBER_EXPORT_SQL, resultSet -> writer.write(CsvCodec.row(List.of(
                nullToEmpty(resultSet.getString("branch_code")),
                nullToEmpty(resultSet.getString("library_card_number")),
                nullToEmpty(resultSet.getString("first_name")),
                nullToEmpty(resultSet.getString("last_name")),
                nullToEmpty(resultSet.getString("email")),
                nullToEmpty(resultSet.getString("phone")),
                nullToEmpty(resultSet.getString("status")),
                dateTimeToString(resultSet.getObject("joined_at", OffsetDateTime.class)),
                dateTimeToString(resultSet.getObject("expires_at", OffsetDateTime.class)),
                nullToEmpty(resultSet.getString("notes"))
        ))));
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

    private void ensureBooksExportBound() {
        ensureExportBound(boundedRowCount("book"));
    }

    private void ensureMembersExportBound() {
        ensureExportBound(boundedRowCount("member"));
    }

    private int boundedRowCount(String table) {
        String sql = switch (table) {
            case "book" -> "SELECT COUNT(*) FROM (SELECT 1 FROM book LIMIT 10001) bounded_rows";
            case "member" -> "SELECT COUNT(*) FROM (SELECT 1 FROM member LIMIT 10001) bounded_rows";
            default -> throw new IllegalArgumentException("Unsupported export table: " + table);
        };
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count == null ? 0 : count;
    }

    private void streamQuery(String sql, ResultSetWriter rowWriter) throws IOException {
        try {
            jdbcTemplate.query(connection -> {
                var statement = connection.prepareStatement(
                        sql,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY
                );
                statement.setFetchSize(EXPORT_FETCH_SIZE);
                statement.setMaxRows(MAX_EXPORT_ROWS);
                return statement;
            }, resultSet -> {
                try {
                    rowWriter.write(resultSet);
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            });
        } catch (UncheckedIOException exception) {
            throw exception.getCause();
        }
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

    private static String integerToString(Integer value) {
        return value == null ? "" : value.toString();
    }

    private static String dateTimeToString(OffsetDateTime value) {
        return value == null ? "" : value.toString();
    }

    @FunctionalInterface
    private interface ResultSetWriter {
        void write(ResultSet resultSet) throws java.sql.SQLException, IOException;
    }

    private static final class ImportState {
        private Map<String, Integer> headers;
        private int imported;
        private final List<String> warnings = new ArrayList<>();
    }
}
