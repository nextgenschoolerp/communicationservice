package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_access_logs", indexes = {
        @Index(name = "idx_document_access_logs_document", columnList = "document_id, occurred_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAccessLog extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id", updatable = false, nullable = false)
    private UUID logId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "actor_name", nullable = false, length = 150)
    private String actorName;

    /** e.g. "Downloaded", "Share link copied", "Access changed to All staff" */
    @Column(name = "action", nullable = false, length = 200)
    private String action;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
