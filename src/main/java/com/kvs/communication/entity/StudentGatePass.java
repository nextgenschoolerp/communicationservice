package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import com.kvs.communication.enums.GatePassStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "student_gate_passes", indexes = {
        @Index(name = "idx_gate_pass_student", columnList = "student_id"),
        @Index(name = "idx_gate_pass_status", columnList = "status"),
        @Index(name = "idx_gate_pass_qr", columnList = "qr_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGatePass extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pass_id", updatable = false, nullable = false)
    private UUID passId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "student_name", nullable = false, length = 150)
    private String studentName;

    @Column(name = "grade", nullable = false, length = 20)
    private String grade;

    @Column(name = "section", nullable = false, length = 10)
    private String section;

    @Column(name = "guardian_id")
    private UUID guardianId;

    @Column(name = "guardian_name", nullable = false, length = 150)
    private String guardianName;

    @Column(name = "pickup_person_name", nullable = false, length = 150)
    private String pickupPersonName;

    @Column(name = "pickup_person_phone", nullable = false, length = 20)
    private String pickupPersonPhone;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "qr_code", nullable = false, length = 100)
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    @Builder.Default
    private GatePassStatus status = GatePassStatus.ISSUED;

    @Column(name = "guardian_approved")
    @Builder.Default
    private Boolean guardianApproved = true;

    @Column(name = "security_verified_by", length = 150)
    private String securityVerifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "exit_time")
    private Instant exitTime;
}
