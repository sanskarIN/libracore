package com.sanskar.libracore.catalog;

import com.sanskar.libracore.audit.AuditService;
import com.sanskar.libracore.catalog.CatalogModels.BookDetail;
import com.sanskar.libracore.catalog.CatalogModels.BookSummary;
import com.sanskar.libracore.catalog.CatalogModels.BranchView;
import com.sanskar.libracore.catalog.CatalogModels.CopyView;
import com.sanskar.libracore.catalog.CatalogModels.CreateBookRequest;
import com.sanskar.libracore.catalog.CatalogModels.CreateBranchRequest;
import com.sanskar.libracore.catalog.CatalogModels.CreateCopyRequest;
import com.sanskar.libracore.catalog.CatalogModels.CreateShelfRequest;
import com.sanskar.libracore.catalog.CatalogModels.SearchPage;
import com.sanskar.libracore.catalog.CatalogModels.ShelfView;
import com.sanskar.libracore.catalog.CatalogModels.UpdateBookRequest;
import com.sanskar.libracore.catalog.CatalogModels.UpdateCopyStatusRequest;
import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.common.TextNormalizer;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class CatalogService {
    private final JdbcClient jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final AuditService auditService;

    public CatalogService(JdbcClient jdbc, NamedParameterJdbcTemplate namedJdbc, AuditService auditService) {
        this.jdbc = jdbc;
        this.namedJdbc = namedJdbc;
        this.auditService = auditService;
    }

    @Transactional
    public BranchView createBranch(CreateBranchRequest request, UUID actorUserId) {
        try {
            ZoneId.of(request.timezone());
        } catch (Exception exception) {
            throw ApiException.badRequest("invalid_timezone", "The branch timezone is not a valid IANA timezone.");
        }

        UUID id = UUID.randomUUID();
        String code = TextNormalizer.display(request.code()).toUpperCase(Locale.ROOT);
        String name = TextNormalizer.display(request.name());

        jdbc.sql("""
                        INSERT INTO branch (id, code, name, timezone)
                        VALUES (:id, :code, :name, :timezone)
                        """)
                .param("id", id)
                .param("code", code)
                .param("name", name)
                .param("timezone", request.timezone())
                .update();
        auditService.success(actorUserId, "BRANCH_CREATE", "BRANCH", id.toString(), null, null);
        return getBranch(id);
    }

    public List<BranchView> listBranches() {
        return jdbc.sql("""
                        SELECT id, code, name, timezone, active, created_at
                        FROM branch
                        ORDER BY active DESC, name, id
                        """)
                .query((rs, rowNum) -> new BranchView(
                        rs.getObject("id", UUID.class),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("timezone"),
                        rs.getBoolean("active"),
                        rs.getObject("created_at", OffsetDateTime.class)
                ))
                .list();
    }

    public BranchView getBranch(UUID id) {
        return jdbc.sql("""
                        SELECT id, code, name, timezone, active, created_at
                        FROM branch
                        WHERE id = :id
                        """)
                .param("id", id)
                .query((rs, rowNum) -> new BranchView(
                        rs.getObject("id", UUID.class),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("timezone"),
                        rs.getBoolean("active"),
                        rs.getObject("created_at", OffsetDateTime.class)
                ))
                .optional()
                .orElseThrow(() -> ApiException.notFound("branch_not_found", "Branch was not found."));
    }

    @Transactional
    public ShelfView createShelf(CreateShelfRequest request, UUID actorUserId) {
        ensureActiveBranch(request.branchId());
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO shelf (id, branch_id, code, label, location_note)
                        VALUES (:id, :branchId, :code, :label, :locationNote)
                        """)
                .param("id", id)
                .param("branchId", request.branchId())
                .param("code", TextNormalizer.display(request.code()).toUpperCase(Locale.ROOT))
                .param("label", TextNormalizer.display(request.label()))
                .param("locationNote", nullableDisplay(request.locationNote()))
                .update();
        auditService.success(actorUserId, "SHELF_CREATE", "SHELF", id.toString(), null, null);
        return getShelf(id);
    }

    public List<ShelfView> listShelves(UUID branchId) {
        if (branchId == null) {
            return jdbc.sql("""
                            SELECT id, branch_id, code, label, location_note, active
                            FROM shelf
                            ORDER BY active DESC, branch_id, code
                            """)
                    .query(CatalogService::mapShelf)
                    .list();
        }
        return jdbc.sql("""
                        SELECT id, branch_id, code, label, location_note, active
                        FROM shelf
                        WHERE branch_id = :branchId
                        ORDER BY active DESC, code
                        """)
                .param("branchId", branchId)
                .query(CatalogService::mapShelf)
                .list();
    }

    public ShelfView getShelf(UUID id) {
        return jdbc.sql("""
                        SELECT id, branch_id, code, label, location_note, active
                        FROM shelf
                        WHERE id = :id
                        """)
                .param("id", id)
                .query(CatalogService::mapShelf)
                .optional()
                .orElseThrow(() -> ApiException.notFound("shelf_not_found", "Shelf was not found."));
    }

    @Transactional
    public BookDetail createBook(CreateBookRequest request, UUID actorUserId) {
        UUID id = UUID.randomUUID();
        String isbn13 = Isbn13.normalizeNullable(request.isbn13());
        UUID publisherId = ensurePublisher(request.publisherName());

        jdbc.sql("""
                        INSERT INTO book (
                            id, isbn13, title, subtitle, description, language_code,
                            publication_year, edition_label, publisher_id
                        ) VALUES (
                            :id, :isbn13, :title, :subtitle, :description, :languageCode,
                            :publicationYear, :editionLabel, :publisherId
                        )
                        """)
                .param("id", id)
                .param("isbn13", isbn13)
                .param("title", TextNormalizer.display(request.title()))
                .param("subtitle", nullableDisplay(request.subtitle()))
                .param("description", nullableText(request.description()))
                .param("languageCode", TextNormalizer.key(request.languageCode()))
                .param("publicationYear", request.publicationYear())
                .param("editionLabel", nullableDisplay(request.editionLabel()))
                .param("publisherId", publisherId)
                .update();

        replaceAuthors(id, request.authors());
        replaceCategories(id, request.categories());
        auditService.success(actorUserId, "BOOK_CREATE", "BOOK", id.toString(), null, null);
        return getBook(id);
    }

    @Transactional
    public BookDetail updateBook(UUID bookId, UpdateBookRequest request, UUID actorUserId) {
        ensureBookExists(bookId);
        UUID publisherId = ensurePublisher(request.publisherName());
        jdbc.sql("""
                        UPDATE book
                        SET title = :title,
                            subtitle = :subtitle,
                            description = :description,
                            language_code = :languageCode,
                            publication_year = :publicationYear,
                            edition_label = :editionLabel,
                            publisher_id = :publisherId,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = :id
                        """)
                .param("title", TextNormalizer.display(request.title()))
                .param("subtitle", nullableDisplay(request.subtitle()))
                .param("description", nullableText(request.description()))
                .param("languageCode", TextNormalizer.key(request.languageCode()))
                .param("publicationYear", request.publicationYear())
                .param("editionLabel", nullableDisplay(request.editionLabel()))
                .param("publisherId", publisherId)
                .param("id", bookId)
                .update();
        replaceAuthors(bookId, request.authors());
        replaceCategories(bookId, request.categories());
        auditService.success(actorUserId, "BOOK_UPDATE", "BOOK", bookId.toString(), null, null);
        return getBook(bookId);
    }

    @Transactional
    public CopyView addCopy(UUID bookId, CreateCopyRequest request, UUID actorUserId) {
        ensureBookExists(bookId);
        ensureActiveBranch(request.branchId());
        if (request.shelfId() != null) {
            ensureShelfBelongsToBranch(request.shelfId(), request.branchId());
        }

        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO book_copy (
                            id, book_id, branch_id, shelf_id, accession_code, barcode_value,
                            qr_value, acquired_on, purchase_price, currency_code, condition_note
                        ) VALUES (
                            :id, :bookId, :branchId, :shelfId, :accessionCode, :barcodeValue,
                            :qrValue, :acquiredOn, :purchasePrice, :currencyCode, :conditionNote
                        )
                        """)
                .param("id", id)
                .param("bookId", bookId)
                .param("branchId", request.branchId())
                .param("shelfId", request.shelfId())
                .param("accessionCode", TextNormalizer.display(request.accessionCode()))
                .param("barcodeValue", nullableDisplay(request.barcodeValue()))
                .param("qrValue", nullableText(request.qrValue()))
                .param("acquiredOn", request.acquiredOn())
                .param("purchasePrice", request.purchasePrice())
                .param("currencyCode", request.currencyCode())
                .param("conditionNote", nullableDisplay(request.conditionNote()))
                .update();
        auditService.success(actorUserId, "COPY_CREATE", "BOOK_COPY", id.toString(), null, null);
        return getCopy(id);
    }

    @Transactional
    public CopyView updateCopyStatus(UUID copyId, UpdateCopyStatusRequest request, UUID actorUserId) {
        CopyView current = getCopy(copyId);
        if ("ON_LOAN".equals(current.status()) || "RESERVED".equals(current.status())) {
            throw ApiException.conflict(
                    "copy_managed_by_circulation",
                    "A copy on loan or reserved must be changed through a circulation workflow."
            );
        }
        if (request.shelfId() != null) {
            ensureShelfBelongsToBranch(request.shelfId(), current.branchId());
        }

        jdbc.sql("""
                        UPDATE book_copy
                        SET status = :status,
                            condition_note = :conditionNote,
                            shelf_id = :shelfId,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = :id
                        """)
                .param("status", request.status())
                .param("conditionNote", nullableDisplay(request.conditionNote()))
                .param("shelfId", request.shelfId())
                .param("id", copyId)
                .update();
        auditService.success(actorUserId, "COPY_STATUS_UPDATE", "BOOK_COPY", copyId.toString(), null, null);
        return getCopy(copyId);
    }

    public SearchPage<BookSummary> searchBooks(
            String rawQuery,
            UUID branchId,
            boolean availableOnly,
            int requestedLimit,
            int requestedOffset
    ) {
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        int offset = Math.max(0, requestedOffset);
        String query = rawQuery == null ? "" : TextNormalizer.key(rawQuery);

        StringBuilder sql = new StringBuilder("""
                SELECT b.id, b.isbn13, b.title, b.subtitle, b.language_code, b.publication_year,
                       b.edition_label, p.name AS publisher_name,
                       (SELECT COUNT(*) FROM book_copy c WHERE c.book_id = b.id) AS total_copies,
                       (SELECT COUNT(*) FROM book_copy c WHERE c.book_id = b.id AND c.status = 'AVAILABLE') AS available_copies
                FROM book b
                LEFT JOIN publisher p ON p.id = b.publisher_id
                WHERE 1 = 1
                """);
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (!query.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(b.title) LIKE :pattern
                        OR LOWER(COALESCE(b.subtitle, '')) LIKE :pattern
                        OR b.isbn13 = :exactQuery
                        OR EXISTS (
                            SELECT 1 FROM book_author ba
                            JOIN author a ON a.id = ba.author_id
                            WHERE ba.book_id = b.id AND a.normalized_name LIKE :pattern
                        )
                        OR EXISTS (
                            SELECT 1 FROM book_category bc
                            JOIN category cat ON cat.id = bc.category_id
                            WHERE bc.book_id = b.id AND cat.normalized_name LIKE :pattern
                        )
                        OR EXISTS (
                            SELECT 1 FROM publisher px
                            WHERE px.id = b.publisher_id AND px.normalized_name LIKE :pattern
                        )
                    )
                    """);
            params.addValue("pattern", "%" + query + "%");
            params.addValue("exactQuery", query.replace("-", "").replace(" ", ""));
        }

        if (branchId != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM book_copy c WHERE c.book_id = b.id AND c.branch_id = :branchId)");
            params.addValue("branchId", branchId);
        }
        if (availableOnly) {
            sql.append(" AND EXISTS (SELECT 1 FROM book_copy c WHERE c.book_id = b.id AND c.status = 'AVAILABLE'");
            if (branchId != null) {
                sql.append(" AND c.branch_id = :branchId");
            }
            sql.append(")");
        }

        sql.append(" ORDER BY LOWER(b.title), b.id LIMIT :fetchLimit OFFSET :offset");
        params.addValue("fetchLimit", limit + 1);
        params.addValue("offset", offset);

        List<BookCoreRow> rows = namedJdbc.query(sql.toString(), params, (rs, rowNum) -> new BookCoreRow(
                rs.getObject("id", UUID.class),
                rs.getString("isbn13"),
                rs.getString("title"),
                rs.getString("subtitle"),
                rs.getString("language_code"),
                rs.getObject("publication_year", Integer.class),
                rs.getString("edition_label"),
                rs.getString("publisher_name"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")
        ));

        boolean hasMore = rows.size() > limit;
        List<BookSummary> items = rows.stream()
                .limit(limit)
                .map(this::toSummary)
                .toList();
        return new SearchPage<>(items, limit, offset, hasMore);
    }

    public BookDetail getBook(UUID bookId) {
        BookCoreRow row = jdbc.sql("""
                        SELECT b.id, b.isbn13, b.title, b.subtitle, b.language_code, b.publication_year,
                               b.edition_label, p.name AS publisher_name,
                               (SELECT COUNT(*) FROM book_copy c WHERE c.book_id = b.id) AS total_copies,
                               (SELECT COUNT(*) FROM book_copy c WHERE c.book_id = b.id AND c.status = 'AVAILABLE') AS available_copies
                        FROM book b
                        LEFT JOIN publisher p ON p.id = b.publisher_id
                        WHERE b.id = :id
                        """)
                .param("id", bookId)
                .query((rs, rowNum) -> new BookCoreRow(
                        rs.getObject("id", UUID.class),
                        rs.getString("isbn13"),
                        rs.getString("title"),
                        rs.getString("subtitle"),
                        rs.getString("language_code"),
                        rs.getObject("publication_year", Integer.class),
                        rs.getString("edition_label"),
                        rs.getString("publisher_name"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies")
                ))
                .optional()
                .orElseThrow(() -> ApiException.notFound("book_not_found", "Book was not found."));

        BookMetadata metadata = jdbc.sql("SELECT description, created_at, updated_at FROM book WHERE id = :id")
                .param("id", bookId)
                .query((rs, rowNum) -> new BookMetadata(
                        rs.getString("description"),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("updated_at", OffsetDateTime.class)
                ))
                .single();
        return new BookDetail(toSummary(row), metadata.description(), listCopiesForBook(bookId), metadata.createdAt(), metadata.updatedAt());
    }

    public CopyView getCopy(UUID copyId) {
        return jdbc.sql(copySelect() + " WHERE c.id = :id")
                .param("id", copyId)
                .query(CatalogService::mapCopy)
                .optional()
                .orElseThrow(() -> ApiException.notFound("copy_not_found", "Book copy was not found."));
    }

    public Optional<CopyView> findCopyByCode(String rawCode) {
        String code = TextNormalizer.display(rawCode);
        if (code.isBlank()) {
            return Optional.empty();
        }
        return jdbc.sql(copySelect() + " WHERE c.barcode_value = :code OR c.accession_code = :code OR c.qr_value = :code ORDER BY c.id LIMIT 1")
                .param("code", code)
                .query(CatalogService::mapCopy)
                .optional();
    }

    public List<CopyView> listCopiesForBook(UUID bookId) {
        return jdbc.sql(copySelect() + " WHERE c.book_id = :bookId ORDER BY br.name, c.accession_code")
                .param("bookId", bookId)
                .query(CatalogService::mapCopy)
                .list();
    }

    private BookSummary toSummary(BookCoreRow row) {
        return new BookSummary(
                row.id(),
                row.isbn13(),
                row.title(),
                row.subtitle(),
                row.languageCode(),
                row.publicationYear(),
                row.editionLabel(),
                row.publisherName(),
                authorsForBook(row.id()),
                categoriesForBook(row.id()),
                row.totalCopies(),
                row.availableCopies()
        );
    }

    private List<String> authorsForBook(UUID bookId) {
        return jdbc.sql("""
                        SELECT a.display_name
                        FROM book_author ba
                        JOIN author a ON a.id = ba.author_id
                        WHERE ba.book_id = :bookId
                        ORDER BY ba.contribution_order, a.display_name
                        """)
                .param("bookId", bookId)
                .query(String.class)
                .list();
    }

    private List<String> categoriesForBook(UUID bookId) {
        return jdbc.sql("""
                        SELECT c.name
                        FROM book_category bc
                        JOIN category c ON c.id = bc.category_id
                        WHERE bc.book_id = :bookId
                        ORDER BY c.name
                        """)
                .param("bookId", bookId)
                .query(String.class)
                .list();
    }

    private UUID ensurePublisher(String rawName) {
        String name = nullableDisplay(rawName);
        if (name == null) {
            return null;
        }
        String normalized = TextNormalizer.key(name);
        Optional<UUID> existing = jdbc.sql("SELECT id FROM publisher WHERE normalized_name = :name")
                .param("name", normalized)
                .query(UUID.class)
                .optional();
        if (existing.isPresent()) {
            return existing.get();
        }
        UUID id = UUID.randomUUID();
        jdbc.sql("INSERT INTO publisher (id, name, normalized_name) VALUES (:id, :name, :normalized)")
                .param("id", id)
                .param("name", name)
                .param("normalized", normalized)
                .update();
        return id;
    }

    private UUID ensureAuthor(String rawName) {
        String displayName = TextNormalizer.display(rawName);
        String normalized = TextNormalizer.key(rawName);
        Optional<UUID> existing = jdbc.sql("SELECT id FROM author WHERE normalized_name = :name")
                .param("name", normalized)
                .query(UUID.class)
                .optional();
        if (existing.isPresent()) {
            return existing.get();
        }
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO author (id, display_name, normalized_name, sort_name)
                        VALUES (:id, :displayName, :normalizedName, :sortName)
                        """)
                .param("id", id)
                .param("displayName", displayName)
                .param("normalizedName", normalized)
                .param("sortName", displayName)
                .update();
        return id;
    }

    private UUID ensureCategory(String rawName) {
        String displayName = TextNormalizer.display(rawName);
        String normalized = TextNormalizer.key(rawName);
        Optional<UUID> existing = jdbc.sql("SELECT id FROM category WHERE normalized_name = :name")
                .param("name", normalized)
                .query(UUID.class)
                .optional();
        if (existing.isPresent()) {
            return existing.get();
        }
        UUID id = UUID.randomUUID();
        jdbc.sql("INSERT INTO category (id, name, normalized_name) VALUES (:id, :name, :normalized)")
                .param("id", id)
                .param("name", displayName)
                .param("normalized", normalized)
                .update();
        return id;
    }

    private void replaceAuthors(UUID bookId, List<String> rawAuthors) {
        jdbc.sql("DELETE FROM book_author WHERE book_id = :bookId")
                .param("bookId", bookId)
                .update();
        List<String> authors = normalizedUnique(rawAuthors);
        for (int index = 0; index < authors.size(); index++) {
            UUID authorId = ensureAuthor(authors.get(index));
            jdbc.sql("""
                            INSERT INTO book_author (book_id, author_id, contribution_order, contribution_role)
                            VALUES (:bookId, :authorId, :position, 'AUTHOR')
                            """)
                    .param("bookId", bookId)
                    .param("authorId", authorId)
                    .param("position", index)
                    .update();
        }
    }

    private void replaceCategories(UUID bookId, List<String> rawCategories) {
        jdbc.sql("DELETE FROM book_category WHERE book_id = :bookId")
                .param("bookId", bookId)
                .update();
        for (String categoryName : normalizedUnique(rawCategories)) {
            UUID categoryId = ensureCategory(categoryName);
            jdbc.sql("INSERT INTO book_category (book_id, category_id) VALUES (:bookId, :categoryId)")
                    .param("bookId", bookId)
                    .param("categoryId", categoryId)
                    .update();
        }
    }

    private static List<String> normalizedUnique(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> seenKeys = new LinkedHashSet<>();
        List<String> result = new ArrayList<>();
        for (String raw : values) {
            String display = TextNormalizer.display(raw);
            String key = TextNormalizer.key(raw);
            if (!display.isBlank() && seenKeys.add(key)) {
                result.add(display);
            }
        }
        return List.copyOf(result);
    }

    private void ensureBookExists(UUID bookId) {
        int count = jdbc.sql("SELECT COUNT(*) FROM book WHERE id = :id")
                .param("id", bookId)
                .query(Integer.class)
                .single();
        if (count == 0) {
            throw ApiException.notFound("book_not_found", "Book was not found.");
        }
    }

    private void ensureActiveBranch(UUID branchId) {
        int count = jdbc.sql("SELECT COUNT(*) FROM branch WHERE id = :id AND active = TRUE")
                .param("id", branchId)
                .query(Integer.class)
                .single();
        if (count == 0) {
            throw ApiException.notFound("branch_not_found", "Active branch was not found.");
        }
    }

    private void ensureShelfBelongsToBranch(UUID shelfId, UUID branchId) {
        int count = jdbc.sql("SELECT COUNT(*) FROM shelf WHERE id = :shelfId AND branch_id = :branchId AND active = TRUE")
                .param("shelfId", shelfId)
                .param("branchId", branchId)
                .query(Integer.class)
                .single();
        if (count == 0) {
            throw ApiException.badRequest("invalid_shelf", "Shelf does not belong to the selected active branch.");
        }
    }

    private static ShelfView mapShelf(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new ShelfView(
                rs.getObject("id", UUID.class),
                rs.getObject("branch_id", UUID.class),
                rs.getString("code"),
                rs.getString("label"),
                rs.getString("location_note"),
                rs.getBoolean("active")
        );
    }

    private static CopyView mapCopy(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new CopyView(
                rs.getObject("id", UUID.class),
                rs.getObject("book_id", UUID.class),
                rs.getObject("branch_id", UUID.class),
                rs.getString("branch_name"),
                rs.getObject("shelf_id", UUID.class),
                rs.getString("shelf_label"),
                rs.getString("accession_code"),
                rs.getString("barcode_value"),
                rs.getString("qr_value"),
                rs.getString("status"),
                rs.getObject("acquired_on", java.time.LocalDate.class),
                rs.getBigDecimal("purchase_price"),
                rs.getString("currency_code"),
                rs.getString("condition_note")
        );
    }

    private static String copySelect() {
        return """
                SELECT c.id, c.book_id, c.branch_id, br.name AS branch_name,
                       c.shelf_id, s.label AS shelf_label, c.accession_code,
                       c.barcode_value, c.qr_value, c.status, c.acquired_on,
                       c.purchase_price, c.currency_code, c.condition_note
                FROM book_copy c
                JOIN branch br ON br.id = c.branch_id
                LEFT JOIN shelf s ON s.id = c.shelf_id
                """;
    }

    private static String nullableDisplay(String value) {
        if (value == null) {
            return null;
        }
        String normalized = TextNormalizer.display(value);
        return normalized.isBlank() ? null : normalized;
    }

    private static String nullableText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private record BookCoreRow(
            UUID id,
            String isbn13,
            String title,
            String subtitle,
            String languageCode,
            Integer publicationYear,
            String editionLabel,
            String publisherName,
            int totalCopies,
            int availableCopies
    ) {
    }

    private record BookMetadata(String description, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    }
}
