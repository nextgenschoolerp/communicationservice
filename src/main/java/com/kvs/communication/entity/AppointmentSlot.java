package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "appointment_slots", indexes = {
        @Index(name = "idx_slot_tenant_date", columnList = "tenant_id, slot_date"),
        @Index(name = "idx_slot_host", columnList = "host_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentSlot extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slot_id", updatable = false, nullable = false)
    private UUID slotId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "host_user_id", nullable = false)
    private String hostUserId;

    @Column(name = "host_name", nullable = false, length = 150)
    private String hostName;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false, length = 10)
    private String startTime; // e.g. "10:00"

    @Column(name = "end_time", nullable = false, length = 10)
    private String endTime; // e.g. "10:30"

    @Column(name = "is_booked")
    @Builder.Default
    private Boolean isBooked = false;

    @Column(name = "booked_by_parent_id")
    private UUID bookedByParentId;

    @Column(name = "booked_by_parent_name", length = 150)
    private String bookedByParentName;
}
