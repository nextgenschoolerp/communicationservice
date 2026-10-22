package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/** Which school alerts a staff member receives (Settings → Notifications). */
@Entity
@Table(name = "staff_alert_preferences", uniqueConstraints = {
        @UniqueConstraint(name = "uk_staff_alert_pref_user", columnNames = {"tenant_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffAlertPreference extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "preference_id", updatable = false, nullable = false)
    private UUID preferenceId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    /** authservice user IDs are text */
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "attendance_alerts", nullable = false)
    @Builder.Default
    private Boolean attendanceAlerts = true;

    @Column(name = "fee_reminders", nullable = false)
    @Builder.Default
    private Boolean feeReminders = true;

    @Column(name = "leave_requests", nullable = false)
    @Builder.Default
    private Boolean leaveRequests = true;

    @Column(name = "daily_digest", nullable = false)
    @Builder.Default
    private Boolean dailyDigest = false;
}
