package com.sanskar.libracore.audit;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {
    private final JdbcClient jdbc;

    public AuditService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void success(UUID actorUserId, String action, String entityType, String entityId, String correlationId, String metadataJson) {
        write(actorUserId, action, entityType, entityId, "SUCCESS", correlationId, metadataJson);
    }

    public void denied(UUID actorUserId, String action, String entityType, String entityId, String correlationId, String metadataJson) {
        write(actorUserId, action, entityType, entityId, "DENIED", correlationId, metadataJson);
    }

    public void failure(UUID actorUserId, String action, String entityType, String entityId, String correlationId, String metadataJson) {
        write(actorUserId, action, entityType, entityId, "FAILURE", correlationId, metadataJson);
    }

    private void write(
            UUID actorUserId,
            String action,
            String entityType,
            String entityId,
            String outcome,
            String correlationId,
            String metadataJson
    ) {
        jdbc.sql("""
                INSERT INTO audit_event (
                    id, actor_user_id, action, entity_type, entity_id, outcome, correlation_id, metadata_json
                ) VALUES (
                    :id, :actorUserId, :action, :entityType, :entityId, :outcome, :correlationId, :metadataJson
                )
                """)
                .param("id", UUID.randomUUID())
                .param("actorUserId", actorUserId)
                .param("action", action)
                .param("entityType", entityType)
                .param("entityId", entityId)
                .param("outcome", outcome)
                .param("correlationId", correlationId)
                .param("metadataJson", metadataJson)
                .update();
    }
}
