package com.sanskar.libracore.security;

import com.sanskar.libracore.audit.AuditService;
import com.sanskar.libracore.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final JdbcClient jdbc;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final Clock clock;
    private final Duration sessionTtl;
    private final String dummyPasswordHash;

    public SessionTokenService(
            JdbcClient jdbc,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            Clock clock,
            @Value("${app.session.ttl-hours:12}") long sessionTtlHours
    ) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.clock = clock;
        this.sessionTtl = Duration.ofHours(Math.max(1, Math.min(sessionTtlHours, 168)));
        this.dummyPasswordHash = passwordEncoder.encode("libra-core-dummy-password-not-an-account");
    }

    @Transactional
    public LoginResult login(String rawEmail, String rawPassword, String userAgent) {
        String email = normalizeEmail(rawEmail);
        String password = rawPassword == null ? "" : rawPassword;

        Optional<UserRow> user = jdbc.sql("""
                        SELECT id, email, password_hash, role, member_id, enabled
                        FROM app_user
                        WHERE LOWER(email) = :email
                        """)
                .param("email", email)
                .query((rs, rowNum) -> new UserRow(
                        rs.getObject("id", UUID.class),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getObject("member_id", UUID.class),
                        rs.getBoolean("enabled")
                ))
                .optional();

        boolean passwordMatches;
        if (user.isPresent()) {
            passwordMatches = passwordEncoder.matches(password, user.get().passwordHash());
        } else {
            passwordEncoder.matches(password, dummyPasswordHash);
            passwordMatches = false;
        }

        if (user.isEmpty() || !user.get().enabled() || !passwordMatches) {
            auditService.denied(null, "AUTH_LOGIN", "APP_USER", null, null, null);
            throw ApiException.unauthorized("invalid_credentials", "The email or password is incorrect.");
        }

        byte[] tokenBytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String tokenHash = sha256Hex(token);

        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        OffsetDateTime expiresAt = now.plus(sessionTtl);
        UUID sessionId = UUID.randomUUID();

        jdbc.sql("""
                        INSERT INTO app_session (
                            id, user_id, token_hash, created_at, last_seen_at, expires_at, user_agent_hash
                        ) VALUES (
                            :id, :userId, :tokenHash, :now, :now, :expiresAt, :userAgentHash
                        )
                        """)
                .param("id", sessionId)
                .param("userId", user.get().id())
                .param("tokenHash", tokenHash)
                .param("now", now)
                .param("expiresAt", expiresAt)
                .param("userAgentHash", hashUserAgent(userAgent))
                .update();

        AppPrincipal principal = new AppPrincipal(
                user.get().id(),
                user.get().email(),
                user.get().role(),
                user.get().memberId()
        );
        auditService.success(principal.userId(), "AUTH_LOGIN", "APP_USER", principal.userId().toString(), null, null);
        return new LoginResult(token, expiresAt, principal);
    }

    @Transactional
    public Optional<AppPrincipal> authenticate(String rawToken) {
        if (rawToken == null || rawToken.length() < 32 || rawToken.length() > 256) {
            return Optional.empty();
        }

        String tokenHash = sha256Hex(rawToken);
        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);

        Optional<AppPrincipal> principal = jdbc.sql("""
                        SELECT u.id, u.email, u.role, u.member_id
                        FROM app_session s
                        JOIN app_user u ON u.id = s.user_id
                        WHERE s.token_hash = :tokenHash
                          AND s.revoked_at IS NULL
                          AND s.expires_at > :now
                          AND u.enabled = TRUE
                        """)
                .param("tokenHash", tokenHash)
                .param("now", now)
                .query((rs, rowNum) -> new AppPrincipal(
                        rs.getObject("id", UUID.class),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getObject("member_id", UUID.class)
                ))
                .optional();

        principal.ifPresent(value -> jdbc.sql("""
                        UPDATE app_session
                        SET last_seen_at = :now
                        WHERE token_hash = :tokenHash
                        """)
                .param("now", now)
                .param("tokenHash", tokenHash)
                .update());

        return principal;
    }

    @Transactional
    public void logout(String rawToken, UUID expectedUserId) {
        if (rawToken == null || expectedUserId == null) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        int changed = jdbc.sql("""
                        UPDATE app_session
                        SET revoked_at = :now
                        WHERE token_hash = :tokenHash
                          AND user_id = :userId
                          AND revoked_at IS NULL
                        """)
                .param("now", now)
                .param("tokenHash", sha256Hex(rawToken))
                .param("userId", expectedUserId)
                .update();

        if (changed > 0) {
            auditService.success(expectedUserId, "AUTH_LOGOUT", "APP_USER", expectedUserId.toString(), null, null);
        }
    }

    @Transactional
    public int revokeExpiredSessions() {
        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        return jdbc.sql("DELETE FROM app_session WHERE expires_at <= :now OR revoked_at IS NOT NULL")
                .param("now", now)
                .update();
    }

    private static String normalizeEmail(String rawEmail) {
        if (rawEmail == null) {
            return "";
        }
        return rawEmail.trim().toLowerCase(Locale.ROOT);
    }

    private static String hashUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        String bounded = userAgent.length() > 1000 ? userAgent.substring(0, 1000) : userAgent;
        return sha256Hex(bounded);
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime", exception);
        }
    }

    public record LoginResult(String token, OffsetDateTime expiresAt, AppPrincipal principal) {
    }

    private record UserRow(
            UUID id,
            String email,
            String passwordHash,
            String role,
            UUID memberId,
            boolean enabled
    ) {
    }
}
