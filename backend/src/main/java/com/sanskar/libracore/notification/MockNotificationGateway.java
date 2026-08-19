package com.sanskar.libracore.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.notifications.mode", havingValue = "MOCK", matchIfMissing = true)
public class MockNotificationGateway implements NotificationGateway {
    private static final Logger log = LoggerFactory.getLogger(MockNotificationGateway.class);

    @Override
    public void send(UUID messageId, String recipient, String subject, String body) {
        log.info("Mock notification delivered messageId={}", messageId);
    }
}
