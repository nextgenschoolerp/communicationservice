package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/** A school's Contact support card on Help & support: how to reach the support desk and when it is open. */
@Entity
@Table(name = "support_contact_settings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_support_contact_tenant", columnNames = {"tenant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportContactSetting extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "setting_id", updatable = false, nullable = false)
    private UUID settingId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    /** Comma separated DayOfWeek names, e.g. MONDAY,TUESDAY */
    @Column(name = "open_days", nullable = false, length = 100)
    private String openDays;

    @Column(name = "opens_at", nullable = false, length = 5)
    private String opensAt;

    @Column(name = "closes_at", nullable = false, length = 5)
    private String closesAt;

    @Column(name = "response_time", nullable = false, length = 100)
    private String responseTime;
}
