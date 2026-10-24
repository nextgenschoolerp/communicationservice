package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device_registrations", indexes = {
        @Index(name = "idx_device_user_id", columnList = "user_id"),
        @Index(name = "idx_device_token", columnList = "device_token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegistration extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "registration_id", updatable = false, nullable = false)
    private UUID registrationId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_token", nullable = false, length = 255)
    private String deviceToken;

    @Column(name = "platform", nullable = false, length = 20)
    private String platform; // IOS, ANDROID

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_active_at")
    @Builder.Default
    private Instant lastActiveAt = Instant.now();
}
