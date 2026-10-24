package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRegistrationResponse {

    private UUID registrationId;
    private String tenantId;
    private UUID userId;
    private String deviceToken;
    private String platform;
    private String appVersion;
    private Boolean isActive;
    private Instant lastActiveAt;
}
