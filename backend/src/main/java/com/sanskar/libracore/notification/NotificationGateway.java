package com.sanskar.libracore.notification;

import java.util.UUID;

public interface NotificationGateway {
    void send(UUID messageId, String recipient, String subject, String body);
}
