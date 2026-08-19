package com.sanskar.libracore.member;

import com.sanskar.libracore.audit.AuditService;
import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.common.TextNormalizer;
import com.sanskar.libracore.member.MemberModels.CreateMemberAccountRequest;
import com.sanskar.libracore.member.MemberModels.CreateMemberRequest;
import com.sanskar.libracore.member.MemberModels.MemberPage;
import com.sanskar.libracore.member.MemberModels.MemberView;
import com.sanskar.libracore.member.MemberModels.UpdateMemberRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class MemberService {
    private final JdbcClient jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public MemberService(
            JdbcClient jdbc,
            NamedParameterJdbcTemplate namedJdbc,
            PasswordEncoder passwordEncoder,
            AuditService auditService
    ) {
        this.jdbc = jdbc;
        this.namedJdbc = namedJdbc;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public MemberView createMember(CreateMemberRequest request, UUID actorUserId) {
        ensureActiveBranch(request.homeBranchId());
        UUID memberId = UUID.randomUUID();
        String email = normalizeEmail(request.email());
        String card = TextNormalizer.display(request.libraryCardNumber()).toUpperCase(Locale.ROOT);

        jdbc.sql("""
                        INSERT INTO member (
                            id, home_branch_id, library_card_number, first_name, last_name,
                            email, phone, expires_at, notes
                        ) VALUES (
                            :id, :branchId, :card, :firstName, :lastName,
                            :email, :phone, :expiresAt, :notes
                        )
                        """)
                .param("id", memberId)
                .param("branchId", request.homeBranchId())
                .param("card", card)
                .param("firstName", TextNormalizer.display(request.firstName()))
                .param("lastName", TextNormalizer.display(request.lastName()))
                .param("email", email)
                .param("phone", nullableDisplay(request.phone()))
                .param("expiresAt", request.expiresAt())
                .param("notes", nullableText(request.notes()))
                .update();

        if (request.accountPassword() != null && !request.accountPassword().isBlank()) {
            createAccountInternal(memberId, email, request.accountPassword());
        }

        auditService.success(actorUserId, "MEMBER_CREATE", "MEMBER", memberId.toString(), null, null);
        return getMember(memberId);
    }

    @Transactional
    public MemberView updateMember(UUID memberId, UpdateMemberRequest request, UUID actorUserId) {
        getMember(memberId);
        ensureActiveBranch(request.homeBranchId());
        String email = normalizeEmail(request.email());

        jdbc.sql("""
                        UPDATE member
                        SET home_branch_id = :branchId,
                            first_name = :firstName,
                            last_name = :lastName,
                            email = :email,
                            phone = :phone,
                            expires_at = :expiresAt,
                            notes = :notes,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = :id
                        """)
                .param("branchId", request.homeBranchId())
                .param("firstName", TextNormalizer.display(request.firstName()))
                .param("lastName", TextNormalizer.display(request.lastName()))
                .param("email", email)
                .param("phone", nullableDisplay(request.phone()))
                .param("expiresAt", request.expiresAt())
                .param("notes", nullableText(request.notes()))
                .param("id", memberId)
                .update();

        jdbc.sql("""
                        UPDATE app_user
                        SET email = :email, updated_at = CURRENT_TIMESTAMP
                        WHERE member_id = :memberId
                        """)
                .param("email", email)
                .param("memberId", memberId)
                .update();

        auditService.success(actorUserId, "MEMBER_UPDATE", "MEMBER", memberId.toString(), null, null);
        return getMember(memberId);
    }

    @Transactional
    public MemberView updateStatus(UUID memberId, String status, UUID actorUserId) {
        MemberView member = getMember(memberId);
        if (member.status().equals(status)) {
            return member;
        }

        if ("CLOSED".equals(status) && (member.openLoanCount() > 0 || member.activeReservationCount() > 0)) {
            throw ApiException.conflict(
                    "member_has_active_circulation",
                    "A member with open loans or active reservations cannot be closed."
            );
        }

        jdbc.sql("UPDATE member SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
                .param("status", status)
                .param("id", memberId)
                .update();
        jdbc.sql("UPDATE app_user SET enabled = :enabled, updated_at = CURRENT_TIMESTAMP WHERE member_id = :memberId")
                .param("enabled", "ACTIVE".equals(status))
                .param("memberId", memberId)
                .update();

        auditService.success(actorUserId, "MEMBER_STATUS_UPDATE", "MEMBER", memberId.toString(), null,
                "{\"status\":\"" + status + "\"}");
        return getMember(memberId);
    }

    @Transactional
    public MemberView createMemberAccount(UUID memberId, CreateMemberAccountRequest request, UUID actorUserId) {
        MemberView member = getMember(memberId);
        if (!"ACTIVE".equals(member.status())) {
            throw ApiException.conflict("member_not_active", "Only an active member can receive a login account.");
        }
        int existing = jdbc.sql("SELECT COUNT(*) FROM app_user WHERE member_id = :memberId")
                .param("memberId", memberId)
                .query(Integer.class)
                .single();
        if (existing > 0) {
            throw ApiException.conflict("member_account_exists", "This member already has a login account.");
        }
        createAccountInternal(memberId, member.email(), request.password());
        auditService.success(actorUserId, "MEMBER_ACCOUNT_CREATE", "MEMBER", memberId.toString(), null, null);
        return getMember(memberId);
    }

    public MemberView getMember(UUID memberId) {
        return jdbc.sql(memberSelect() + " WHERE m.id = :id")
                .param("id", memberId)
                .query(MemberService::mapMember)
                .optional()
                .orElseThrow(() -> ApiException.notFound("member_not_found", "Member was not found."));
    }

    public MemberPage searchMembers(
            String rawQuery,
            UUID branchId,
            String status,
            int requestedLimit,
            int requestedOffset
    ) {
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        int offset = Math.max(0, requestedOffset);
        String query = rawQuery == null ? "" : TextNormalizer.key(rawQuery);

        StringBuilder sql = new StringBuilder(memberSelect() + " WHERE 1 = 1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (!query.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(m.first_name) LIKE :pattern
                        OR LOWER(m.last_name) LIKE :pattern
                        OR LOWER(m.email) LIKE :pattern
                        OR LOWER(m.library_card_number) LIKE :pattern
                    )
                    """);
            params.addValue("pattern", "%" + query + "%");
        }
        if (branchId != null) {
            sql.append(" AND m.home_branch_id = :branchId");
            params.addValue("branchId", branchId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND m.status = :status");
            params.addValue("status", status);
        }
        sql.append(" ORDER BY LOWER(m.last_name), LOWER(m.first_name), m.id LIMIT :fetchLimit OFFSET :offset");
        params.addValue("fetchLimit", limit + 1);
        params.addValue("offset", offset);

        List<MemberView> rows = namedJdbc.query(sql.toString(), params, MemberService::mapMember);
        boolean hasMore = rows.size() > limit;
        return new MemberPage(rows.stream().limit(limit).toList(), limit, offset, hasMore);
    }

    private void createAccountInternal(UUID memberId, String email, String password) {
        UUID userId = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO app_user (id, email, password_hash, role, member_id, enabled)
                        VALUES (:id, :email, :passwordHash, 'MEMBER', :memberId, TRUE)
                        """)
                .param("id", userId)
                .param("email", normalizeEmail(email))
                .param("passwordHash", passwordEncoder.encode(password))
                .param("memberId", memberId)
                .update();
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

    private static String memberSelect() {
        return """
                SELECT m.id, m.home_branch_id, b.name AS home_branch_name, m.library_card_number,
                       m.first_name, m.last_name, m.email, m.phone, m.status, m.joined_at,
                       m.expires_at, m.notes,
                       (SELECT COUNT(*) FROM loan l WHERE l.member_id = m.id AND l.status = 'OPEN') AS open_loan_count,
                       (SELECT COUNT(*) FROM reservation r WHERE r.member_id = m.id AND r.status IN ('WAITING','READY')) AS active_reservation_count,
                       COALESCE((SELECT SUM(f.amount) FROM fine_charge f WHERE f.member_id = m.id AND f.status = 'OUTSTANDING'), 0) AS outstanding_fine,
                       EXISTS(SELECT 1 FROM app_user u WHERE u.member_id = m.id AND u.enabled = TRUE) AS account_enabled
                FROM member m
                JOIN branch b ON b.id = m.home_branch_id
                """;
    }

    private static MemberView mapMember(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        BigDecimal fine = rs.getBigDecimal("outstanding_fine");
        return new MemberView(
                rs.getObject("id", UUID.class),
                rs.getObject("home_branch_id", UUID.class),
                rs.getString("home_branch_name"),
                rs.getString("library_card_number"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("status"),
                rs.getObject("joined_at", OffsetDateTime.class),
                rs.getObject("expires_at", OffsetDateTime.class),
                rs.getString("notes"),
                rs.getInt("open_loan_count"),
                rs.getInt("active_reservation_count"),
                fine == null ? BigDecimal.ZERO : fine,
                rs.getBoolean("account_enabled")
        );
    }

    private static String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
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
}
