package com.sanskar.libracore.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class NotificationService {
    private static final int MAX_ATTEMPTS = 5;

    private final JdbcClient jdbc;
    private final NotificationGateway gateway;
    private final Clock clock;
    private final String channel;

    public NotificationService(
            JdbcClient jdbc,
            NotificationGateway gateway,
            Clock clock,
            @Value("${app.notifications.mode:MOCK}") String mode
    ) {
        this.jdbc = jdbc;
        this.gateway = gateway;
        this.clock = clock;
        this.channel = "EMAIL".equalsIgnoreCase(mode) ? "EMAIL" : "MOCK";
    }

    @Transactional
    public UUID enqueueEmail(String recipient, String templateKey, String subject, String body) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO notification_outbox (
                            id, channel, recipient, template_key, payload_json, subject, body,
                            status, attempt_count, next_attempt_at
                        ) VALUES (
                            :id, :channel, :recipient, :templateKey, '{}', :subject, :body,
                            'PENDING', 0, :now
                        )
                        """)
                .param("id", id)
                .param("channel", channel)
                .param("recipient", normalizeEmail(recipient))
                .param("templateKey", bounded(templateKey, 120))
                .param("subject", bounded(subject, 300))
                .param("body", body == null ? "" : body)
                .param("now", now())
                .update();
        return id;
    }

    @Scheduled(fixedDelayString = "${app.notifications.dispatch-interval-ms:5000}")
    @Transactional
    public void dispatchPending() {
        OffsetDateTime now = now();
        List<OutboxRow> rows = jdbc.sql("""
                        SELECT id, recipient, subject, body, attempt_count
                        FROM notification_outbox
                        WHERE status = 'PENDING'
                          AND next_attempt_at <= :now
                        ORDER BY created_at, id
                        LIMIT 20
                        FOR UPDATE SKIP LOCKED
                        """)
                .param("now", now)
                .query((rs, rowNum) -> new OutboxRow(
                        rs.getObject("id", UUID.class),
                        rs.getString("recipient"),
                        rs.getString("subject"),
                        rs.getString("body"),
                        rs.getInt("attempt_count")
                ))
                .list();

        for (OutboxRow row : rows) {
            try {
                gateway.send(row.id(), row.recipient(), row.subject(), row.body());
                jdbc.sql("""
                                UPDATE notification_outbox
                                SET status = 'SENT', sent_at = :now, updated_at = :now,
                                    attempt_count = attempt_count + 1, last_error_code = NULL
                                WHERE id = :id
                                """)
                        .param("now", now)
                        .param("id", row.id())
                        .update();
            } catch (RuntimeException exception) {
                int nextAttempt = row.attemptCount() + 1;
                boolean exhausted = nextAttempt >= MAX_ATTEMPTS;
                long delayMinutes = Math.min(60, 1L << Math.min(nextAttempt, 6));
                jdbc.sql("""
                                UPDATE notification_outbox
                                SET status = :status,
                                    attempt_count = :attemptCount,
                                    next_attempt_at = :nextAttemptAt,
                                    last_error_code = :errorCode,
                                    updated_at = :now
                                WHERE id = :id
                                """)
                        .param("status", exhausted ? "FAILED" : "PENDING")
                        .param("attemptCount", nextAttempt)
                        .param("nextAttemptAt", now.plusMinutes(delayMinutes))
                        .param("errorCode", bounded(exception.getClass().getSimpleName(), 120))
                        .param("now", now)
                        .param("id", row.id())
                        .update();
            }
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private static String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String bounded(String value, int max) {
        String safe = value == null ? "" : value.trim();
        return safe.length() <= max ? safe : safe.substring(0, max);
    }

    private record OutboxRow(UUID id, String recipient, String subject, String body, int attemptCount) {
    }
}
