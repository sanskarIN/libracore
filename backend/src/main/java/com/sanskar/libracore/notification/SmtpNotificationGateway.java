package com.sanskar.libracore.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.notifications.mode", havingValue = "EMAIL")
public class SmtpNotificationGateway implements NotificationGateway {
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpNotificationGateway(
            JavaMailSender mailSender,
            @Value("${app.notifications.from:no-reply@example.invalid}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(UUID messageId, String recipient, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
