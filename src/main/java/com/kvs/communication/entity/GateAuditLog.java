package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gate_audit_logs", indexes = {
        @Index(name = "idx_gate_audit_tenant_time", columnList = "tenant_id, timestamp"),
        @Index(name = "idx_gate_audit_event", columnList = "event_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateAuditLog extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id", updatable = false, nullable = false)
    private UUID auditId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "gate_id", nullable = false, length = 50)
    private String gateId;

    @Column(name = "gate_name", nullable = false, length = 100)
    private String gateName;

    @Column(name = "pass_id")
    private UUID passId;

    @Column(name = "pass_type", nullable = false, length = 30)
    private String passType; // STUDENT_GATE_PASS, VISITOR_PASS

    @Column(name = "person_name", nullable = false, length = 150)
    private String personName;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType; // ENTRY, EXIT, UNAUTHORIZED_ATTEMPT

    @Column(name = "outcome", nullable = false, length = 20)
    private String outcome; // GRANTED, DENIED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private Instant timestamp = Instant.now();
}
