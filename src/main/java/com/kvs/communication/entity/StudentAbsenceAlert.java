package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import com.kvs.communication.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "student_absence_alerts", indexes = {
        @Index(name = "idx_absence_tenant_student", columnList = "tenant_id, student_id"),
        @Index(name = "idx_absence_date", columnList = "absence_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAbsenceAlert extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "alert_id", updatable = false, nullable = false)
    private UUID alertId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "student_name", nullable = false, length = 150)
    private String studentName;

    @Column(name = "guardian_phone", nullable = false, length = 20)
    private String guardianPhone;

    @Column(name = "guardian_name", nullable = false, length = 150)
    private String guardianName;

    @Column(name = "absence_date", nullable = false)
    private LocalDate absenceDate;

    @Column(name = "alert_channel", length = 20)
    @Builder.Default
    private String alertChannel = "WHATSAPP";

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", length = 20)
    @Builder.Default
    private DeliveryStatus deliveryStatus = DeliveryStatus.SENT;

    @Column(name = "escalated")
    @Builder.Default
    private Boolean escalated = false;

    @Column(name = "escalated_at")
    private Instant escalatedAt;
}
