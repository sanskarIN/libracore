package com.sanskar.libracore.circulation;

import com.sanskar.libracore.audit.AuditService;
import com.sanskar.libracore.circulation.FineModels.FineChargeView;
import com.sanskar.libracore.circulation.FineModels.FinePage;
import com.sanskar.libracore.circulation.FineModels.SettleFineRequest;
import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.common.TextNormalizer;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class FineService {
    private final JdbcClient jdbc;
    private final AuditService auditService;
    private final Clock clock;

    public FineService(JdbcClient jdbc, AuditService auditService, Clock clock) {
        this.jdbc = jdbc;
        this.auditService = auditService;
        this.clock = clock;
    }

    public FineChargeView getFine(UUID fineId) {
        return jdbc.sql(fineSelect() + " WHERE f.id = :id")
                .param("id", fineId)
                .query(FineService::mapFine)
                .optional()
                .orElseThrow(() -> ApiException.notFound("fine_not_found", "Fine charge was not found."));
    }

    public FinePage listFines(UUID memberId, String status, int requestedLimit, int requestedOffset) {
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        int offset = Math.max(0, requestedOffset);
        StringBuilder sql = new StringBuilder(fineSelect() + " WHERE f.member_id = :memberId");
        if (status != null && !status.isBlank()) {
            sql.append(" AND f.status = :status");
        }
        sql.append(" ORDER BY f.assessed_at DESC, f.id LIMIT :fetchLimit OFFSET :offset");

        JdbcClient.StatementSpec statement = jdbc.sql(sql.toString())
                .param("memberId", memberId)
                .param("fetchLimit", limit + 1)
                .param("offset", offset);
        if (status != null && !status.isBlank()) {
            statement = statement.param("status", status);
        }
        List<FineChargeView> rows = statement.query(FineService::mapFine).list();
        boolean hasMore = rows.size() > limit;
        return new FinePage(rows.stream().limit(limit).toList(), limit, offset, hasMore);
    }

    @Transactional
    public FineChargeView settle(UUID fineId, SettleFineRequest request, UUID actorUserId) {
        FineChargeView fine = jdbc.sql(fineSelect() + " WHERE f.id = :id FOR UPDATE")
                .param("id", fineId)
                .query(FineService::mapFine)
                .optional()
                .orElseThrow(() -> ApiException.notFound("fine_not_found", "Fine charge was not found."));
        if (!"OUTSTANDING".equals(fine.status())) {
            throw ApiException.conflict("fine_already_settled", "Only an outstanding fine can be settled.");
        }

        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        String note = request.note() == null ? null : TextNormalizer.display(request.note());
        if (note != null && note.isBlank()) {
            note = null;
        }
        jdbc.sql("""
                        UPDATE fine_charge
                        SET status = :status,
                            settled_at = :settledAt,
                            settled_by_user_id = :actorUserId,
                            waived_by_user_id = CASE WHEN :status = 'WAIVED' THEN :actorUserId ELSE NULL END,
                            settlement_note = :note
                        WHERE id = :id
                        """)
                .param("status", request.status())
                .param("settledAt", now)
                .param("actorUserId", actorUserId)
                .param("note", note)
                .param("id", fineId)
                .update();

        auditService.success(actorUserId, "FINE_" + request.status(), "FINE_CHARGE", fineId.toString(), null, null);
        return getFine(fineId);
    }

    private static String fineSelect() {
        return """
                SELECT f.id, f.loan_id, f.member_id, m.library_card_number,
                       (m.first_name || ' ' || m.last_name) AS member_name,
                       c.book_id, b.title AS book_title, f.amount, f.currency_code,
                       f.status, f.reason, f.assessed_at, f.settled_at, f.settlement_note
                FROM fine_charge f
                JOIN member m ON m.id = f.member_id
                JOIN loan l ON l.id = f.loan_id
                JOIN book_copy c ON c.id = l.copy_id
                JOIN book b ON b.id = c.book_id
                """;
    }

    private static FineChargeView mapFine(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new FineChargeView(
                rs.getObject("id", UUID.class),
                rs.getObject("loan_id", UUID.class),
                rs.getObject("member_id", UUID.class),
                rs.getString("library_card_number"),
                rs.getString("member_name"),
                rs.getObject("book_id", UUID.class),
                rs.getString("book_title"),
                rs.getBigDecimal("amount"),
                rs.getString("currency_code"),
                rs.getString("status"),
                rs.getString("reason"),
                rs.getObject("assessed_at", OffsetDateTime.class),
                rs.getObject("settled_at", OffsetDateTime.class),
                rs.getString("settlement_note")
        );
    }
}
