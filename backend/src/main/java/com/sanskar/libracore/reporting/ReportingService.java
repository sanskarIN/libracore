package com.sanskar.libracore.reporting;

import com.sanskar.libracore.reporting.ReportingModels.AuditEventView;
import com.sanskar.libracore.reporting.ReportingModels.DashboardView;
import com.sanskar.libracore.reporting.ReportingModels.OverdueLoanView;
import com.sanskar.libracore.reporting.ReportingModels.Page;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ReportingService {
    private final JdbcClient jdbc;
    private final Clock clock;

    public ReportingService(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    public DashboardView dashboard(UUID branchId) {
        OffsetDateTime now = now();
        long books = scalarCount("SELECT COUNT(*) FROM book");
        long copies = branchId == null
                ? scalarCount("SELECT COUNT(*) FROM book_copy")
                : scalarCount("SELECT COUNT(*) FROM book_copy WHERE branch_id = :branchId", branchId);
        long availableCopies = branchId == null
                ? scalarCount("SELECT COUNT(*) FROM book_copy WHERE status = 'AVAILABLE'")
                : scalarCount("SELECT COUNT(*) FROM book_copy WHERE branch_id = :branchId AND status = 'AVAILABLE'", branchId);
        long openLoans = branchId == null
                ? scalarCount("SELECT COUNT(*) FROM loan WHERE status = 'OPEN'")
                : scalarCount("""
                        SELECT COUNT(*) FROM loan l
                        JOIN book_copy c ON c.id = l.copy_id
                        WHERE l.status = 'OPEN' AND c.branch_id = :branchId
                        """, branchId);
        long overdueLoans = branchId == null
                ? jdbc.sql("SELECT COUNT(*) FROM loan WHERE status = 'OPEN' AND due_at < :now")
                    .param("now", now).query(Long.class).single()
                : jdbc.sql("""
                        SELECT COUNT(*) FROM loan l
                        JOIN book_copy c ON c.id = l.copy_id
                        WHERE l.status = 'OPEN' AND l.due_at < :now AND c.branch_id = :branchId
                        """).param("now", now).param("branchId", branchId).query(Long.class).single();
        long activeMembers = branchId == null
                ? scalarCount("SELECT COUNT(*) FROM member WHERE status = 'ACTIVE'")
                : scalarCount("SELECT COUNT(*) FROM member WHERE status = 'ACTIVE' AND home_branch_id = :branchId", branchId);
        long waitingReservations = reservationCount(branchId, "WAITING");
        long readyReservations = reservationCount(branchId, "READY");

        FineTotal fine = branchId == null
                ? jdbc.sql("""
                        SELECT COALESCE(SUM(amount), 0) AS total,
                               COALESCE(MIN(currency_code), 'INR') AS currency
                        FROM fine_charge
                        WHERE status = 'OUTSTANDING'
                        """).query((rs, rowNum) -> new FineTotal(rs.getBigDecimal("total"), rs.getString("currency"))).single()
                : jdbc.sql("""
                        SELECT COALESCE(SUM(f.amount), 0) AS total,
                               COALESCE(MIN(f.currency_code), 'INR') AS currency
                        FROM fine_charge f
                        JOIN loan l ON l.id = f.loan_id
                        JOIN book_copy c ON c.id = l.copy_id
                        WHERE f.status = 'OUTSTANDING' AND c.branch_id = :branchId
                        """).param("branchId", branchId)
                    .query((rs, rowNum) -> new FineTotal(rs.getBigDecimal("total"), rs.getString("currency"))).single();

        return new DashboardView(
                books,
                copies,
                availableCopies,
                openLoans,
                overdueLoans,
                activeMembers,
                waitingReservations,
                readyReservations,
                fine.total() == null ? BigDecimal.ZERO : fine.total(),
                fine.currency(),
                now
        );
    }

    public Page<OverdueLoanView> overdueLoans(UUID branchId, int requestedLimit, int requestedOffset) {
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        int offset = Math.max(0, requestedOffset);
        OffsetDateTime now = now();
        StringBuilder sql = new StringBuilder("""
                SELECT l.id AS loan_id, l.copy_id, c.accession_code, b.title AS book_title,
                       l.member_id, m.library_card_number,
                       (m.first_name || ' ' || m.last_name) AS member_name,
                       m.email AS member_email, l.issued_at, l.due_at,
                       c.branch_id, br.name AS branch_name
                FROM loan l
                JOIN book_copy c ON c.id = l.copy_id
                JOIN book b ON b.id = c.book_id
                JOIN member m ON m.id = l.member_id
                JOIN branch br ON br.id = c.branch_id
                WHERE l.status = 'OPEN' AND l.due_at < :now
                """);
        if (branchId != null) {
            sql.append(" AND c.branch_id = :branchId");
        }
        sql.append(" ORDER BY l.due_at, l.id LIMIT :fetchLimit OFFSET :offset");

        JdbcClient.StatementSpec statement = jdbc.sql(sql.toString())
                .param("now", now)
                .param("fetchLimit", limit + 1)
                .param("offset", offset);
        if (branchId != null) {
            statement = statement.param("branchId", branchId);
        }
        List<OverdueLoanView> rows = statement.query((rs, rowNum) -> {
            OffsetDateTime dueAt = rs.getObject("due_at", OffsetDateTime.class);
            long lateSeconds = Math.max(0, Duration.between(dueAt, now).getSeconds());
            long overdueDays = (lateSeconds + 86_399L) / 86_400L;
            return new OverdueLoanView(
                    rs.getObject("loan_id", UUID.class),
                    rs.getObject("copy_id", UUID.class),
                    rs.getString("accession_code"),
                    rs.getString("book_title"),
                    rs.getObject("member_id", UUID.class),
                    rs.getString("library_card_number"),
                    rs.getString("member_name"),
                    rs.getString("member_email"),
                    rs.getObject("issued_at", OffsetDateTime.class),
                    dueAt,
                    overdueDays,
                    rs.getObject("branch_id", UUID.class),
                    rs.getString("branch_name")
            );
        }).list();
        boolean hasMore = rows.size() > limit;
        return new Page<>(rows.stream().limit(limit).toList(), limit, offset, hasMore);
    }

    public Page<AuditEventView> auditEvents(
            String action,
            String entityType,
            String outcome,
            int requestedLimit,
            int requestedOffset
    ) {
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        int offset = Math.max(0, requestedOffset);
        StringBuilder sql = new StringBuilder("""
                SELECT a.id, a.occurred_at, a.actor_user_id, u.email AS actor_email,
                       a.action, a.entity_type, a.entity_id, a.outcome, a.correlation_id
                FROM audit_event a
                LEFT JOIN app_user u ON u.id = a.actor_user_id
                WHERE 1 = 1
                """);
        if (action != null && !action.isBlank()) {
            sql.append(" AND a.action = :action");
        }
        if (entityType != null && !entityType.isBlank()) {
            sql.append(" AND a.entity_type = :entityType");
        }
        if (outcome != null && !outcome.isBlank()) {
            sql.append(" AND a.outcome = :outcome");
        }
        sql.append(" ORDER BY a.occurred_at DESC, a.id LIMIT :fetchLimit OFFSET :offset");

        JdbcClient.StatementSpec statement = jdbc.sql(sql.toString())
                .param("fetchLimit", limit + 1)
                .param("offset", offset);
        if (action != null && !action.isBlank()) {
            statement = statement.param("action", action);
        }
        if (entityType != null && !entityType.isBlank()) {
            statement = statement.param("entityType", entityType);
        }
        if (outcome != null && !outcome.isBlank()) {
            statement = statement.param("outcome", outcome);
        }

        List<AuditEventView> rows = statement.query((rs, rowNum) -> new AuditEventView(
                rs.getObject("id", UUID.class),
                rs.getObject("occurred_at", OffsetDateTime.class),
                rs.getObject("actor_user_id", UUID.class),
                rs.getString("actor_email"),
                rs.getString("action"),
                rs.getString("entity_type"),
                rs.getString("entity_id"),
                rs.getString("outcome"),
                rs.getString("correlation_id")
        )).list();
        boolean hasMore = rows.size() > limit;
        return new Page<>(rows.stream().limit(limit).toList(), limit, offset, hasMore);
    }

    private long scalarCount(String sql) {
        return jdbc.sql(sql).query(Long.class).single();
    }

    private long scalarCount(String sql, UUID branchId) {
        return jdbc.sql(sql).param("branchId", branchId).query(Long.class).single();
    }

    private long reservationCount(UUID branchId, String status) {
        if (branchId == null) {
            return jdbc.sql("SELECT COUNT(*) FROM reservation WHERE status = :status")
                    .param("status", status).query(Long.class).single();
        }
        return jdbc.sql("SELECT COUNT(*) FROM reservation WHERE status = :status AND pickup_branch_id = :branchId")
                .param("status", status)
                .param("branchId", branchId)
                .query(Long.class)
                .single();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private record FineTotal(BigDecimal total, String currency) {
    }
}
