package com.sanskar.libracore.security;

import com.sanskar.libracore.audit.AuditService;
import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.security.AdminUserModels.CreateStaffUserRequest;
import com.sanskar.libracore.security.AdminUserModels.StaffUserPage;
import com.sanskar.libracore.security.AdminUserModels.StaffUserView;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminUserService {
    private final JdbcClient jdbc;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final Clock clock;

    public AdminUserService(
            JdbcClient jdbc,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            Clock clock
    ) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public StaffUserView create(CreateStaffUserRequest request, UUID actorUserId) {
        String email = normalizeEmail(request.email());
        int exists = jdbc.sql("SELECT COUNT(*) FROM app_user WHERE LOWER(email) = :email")
                .param("email", email)
                .query(Integer.class)
                .single();
        if (exists > 0) {
            throw ApiException.conflict("user_email_exists", "An account already uses this email address.");
        }

        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO app_user (id, email, password_hash, role, member_id, enabled)
                        VALUES (:id, :email, :passwordHash, :role, NULL, TRUE)
                        """)
                .param("id", id)
                .param("email", email)
                .param("passwordHash", passwordEncoder.encode(request.password()))
                .param("role", request.role())
                .update();
        auditService.success(actorUserId, "STAFF_USER_CREATE", "APP_USER", id.toString(), null,
                "{\"role\":\"" + request.role() + "\"}");
        return get(id);
    }

    public StaffUserView get(UUID userId) {
        return jdbc.sql(staffSelect() + " WHERE u.id = :id AND u.role IN ('ADMIN','LIBRARIAN')")
                .param("id", userId)
                .query(AdminUserService::mapStaff)
                .optional()
                .orElseThrow(() -> ApiException.notFound("staff_user_not_found", "Staff account was not found."));
    }

    public StaffUserPage list(String role, int requestedLimit, int requestedOffset) {
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        int offset = Math.max(0, requestedOffset);
        StringBuilder sql = new StringBuilder(staffSelect() + " WHERE u.role IN ('ADMIN','LIBRARIAN')");
        if (role != null && !role.isBlank()) {
            sql.append(" AND u.role = :role");
        }
        sql.append(" ORDER BY u.enabled DESC, u.role, LOWER(u.email), u.id LIMIT :fetchLimit OFFSET :offset");

        JdbcClient.StatementSpec statement = jdbc.sql(sql.toString())
                .param("fetchLimit", limit + 1)
                .param("offset", offset);
        if (role != null && !role.isBlank()) {
            statement = statement.param("role", role);
        }
        List<StaffUserView> rows = statement.query(AdminUserService::mapStaff).list();
        boolean hasMore = rows.size() > limit;
        return new StaffUserPage(rows.stream().limit(limit).toList(), limit, offset, hasMore);
    }

    @Transactional
    public StaffUserView setEnabled(UUID userId, boolean enabled, UUID actorUserId) {
        StaffUserView target = lockStaff(userId);
        if (target.id().equals(actorUserId) && !enabled) {
            throw ApiException.conflict("cannot_disable_self", "An administrator cannot disable their own account.");
        }
        if (!enabled && "ADMIN".equals(target.role())) {
            long enabledAdmins = jdbc.sql("SELECT COUNT(*) FROM app_user WHERE role = 'ADMIN' AND enabled = TRUE")
                    .query(Long.class)
                    .single();
            if (enabledAdmins <= 1) {
                throw ApiException.conflict("last_admin_required", "At least one enabled administrator account must remain.");
            }
        }

        jdbc.sql("UPDATE app_user SET enabled = :enabled, updated_at = :now WHERE id = :id")
                .param("enabled", enabled)
                .param("now", now())
                .param("id", userId)
                .update();
        if (!enabled) {
            revokeSessions(userId);
        }
        auditService.success(actorUserId, enabled ? "STAFF_USER_ENABLE" : "STAFF_USER_DISABLE",
                "APP_USER", userId.toString(), null, null);
        return get(userId);
    }

    @Transactional
    public StaffUserView resetPassword(UUID userId, String password, UUID actorUserId) {
        StaffUserView target = lockStaff(userId);
        jdbc.sql("""
                        UPDATE app_user
                        SET password_hash = :passwordHash, updated_at = :now
                        WHERE id = :id
                        """)
                .param("passwordHash", passwordEncoder.encode(password))
                .param("now", now())
                .param("id", target.id())
                .update();
        revokeSessions(target.id());
        auditService.success(actorUserId, "STAFF_PASSWORD_RESET", "APP_USER", target.id().toString(), null, null);
        return get(target.id());
    }

    private StaffUserView lockStaff(UUID userId) {
        return jdbc.sql("""
                        SELECT u.id, u.email, u.role, u.enabled, u.created_at, u.updated_at,
                               (SELECT COUNT(*) FROM app_session s
                                WHERE s.user_id = u.id
                                  AND s.revoked_at IS NULL
                                  AND s.expires_at > :now) AS active_session_count
                        FROM app_user u
                        WHERE u.id = :id AND u.role IN ('ADMIN','LIBRARIAN')
                        FOR UPDATE
                        """)
                .param("now", now())
                .param("id", userId)
                .query(AdminUserService::mapStaff)
                .optional()
                .orElseThrow(() -> ApiException.notFound("staff_user_not_found", "Staff account was not found."));
    }

    private void revokeSessions(UUID userId) {
        jdbc.sql("""
                        UPDATE app_session
                        SET revoked_at = :now
                        WHERE user_id = :userId AND revoked_at IS NULL
                        """)
                .param("now", now())
                .param("userId", userId)
                .update();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private static String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String staffSelect() {
        return """
                SELECT u.id, u.email, u.role, u.enabled, u.created_at, u.updated_at,
                       (SELECT COUNT(*) FROM app_session s
                        WHERE s.user_id = u.id
                          AND s.revoked_at IS NULL
                          AND s.expires_at > CURRENT_TIMESTAMP) AS active_session_count
                FROM app_user u
                """;
    }

    private static StaffUserView mapStaff(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new StaffUserView(
                rs.getObject("id", UUID.class),
                rs.getString("email"),
                rs.getString("role"),
                rs.getBoolean("enabled"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class),
                rs.getInt("active_session_count")
        );
    }
}
