package com.sanskar.libracore.security;

import com.sanskar.libracore.audit.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.UUID;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final JdbcClient jdbc;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final String configuredEmail;
    private final String configuredPassword;

    public AdminBootstrap(
            JdbcClient jdbc,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            @Value("${app.bootstrap-admin.email:}") String configuredEmail,
            @Value("${app.bootstrap-admin.password:}") String configuredPassword
    ) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.configuredEmail = configuredEmail;
        this.configuredPassword = configuredPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean hasEmail = StringUtils.hasText(configuredEmail);
        boolean hasPassword = StringUtils.hasText(configuredPassword);

        if (!hasEmail && !hasPassword) {
            return;
        }
        if (!hasEmail || !hasPassword) {
            log.warn("Admin bootstrap requires both email and password; bootstrap skipped.");
            return;
        }
        if (configuredPassword.length() < 12) {
            log.warn("Admin bootstrap password must be at least 12 characters; bootstrap skipped.");
            return;
        }

        String email = configuredEmail.trim().toLowerCase(Locale.ROOT);
        boolean exists = jdbc.sql("SELECT COUNT(*) FROM app_user WHERE LOWER(email) = :email")
                .param("email", email)
                .query(Integer.class)
                .single() > 0;
        if (exists) {
            return;
        }

        UUID userId = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO app_user (id, email, password_hash, role, enabled)
                        VALUES (:id, :email, :passwordHash, 'ADMIN', TRUE)
                        """)
                .param("id", userId)
                .param("email", email)
                .param("passwordHash", passwordEncoder.encode(configuredPassword))
                .update();

        auditService.success(userId, "ADMIN_BOOTSTRAP", "APP_USER", userId.toString(), null, null);
        log.info("Created configured LibraCore bootstrap administrator account.");
    }
}
