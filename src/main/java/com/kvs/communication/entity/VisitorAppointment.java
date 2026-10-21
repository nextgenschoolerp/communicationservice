package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "visitor_appointments", indexes = {
        @Index(name = "idx_visitor_tenant", columnList = "tenant_id"),
        @Index(name = "idx_visitor_status", columnList = "status"),
        @Index(name = "idx_visitor_appointment_time", columnList = "appointment_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorAppointment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appointment_id", updatable = false, nullable = false)
    private UUID appointmentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "visitor_name", nullable = false, length = 150)
    private String visitorName;

    @Column(name = "visitor_phone", nullable = false, length = 20)
    private String visitorPhone;

    @Column(name = "visitor_id_type", length = 50)
    private String visitorIdType;

    @Column(name = "visitor_id_number", length = 50)
    private String visitorIdNumber;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "host_staff_id")
    private UUID hostStaffId;

    @Column(name = "host_staff_name", nullable = false, length = 150)
    private String hostStaffName;

    @Column(name = "appointment_time", nullable = false)
    private Instant appointmentTime;

    @Column(name = "check_in_time")
    private Instant checkInTime;

    @Column(name = "check_out_time")
    private Instant checkOutTime;

    @Column(name = "badge_number", length = 50)
    private String badgeNumber;

    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "SCHEDULED"; // SCHEDULED, CHECKED_IN, CHECKED_OUT, CANCELLED
}
