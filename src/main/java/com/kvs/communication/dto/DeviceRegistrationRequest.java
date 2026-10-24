package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRegistrationRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Device token is required")
    @Size(max = 255)
    private String deviceToken;

    @NotBlank(message = "Platform is required (IOS or ANDROID)")
    @Size(max = 20)
    private String platform;

    @Size(max = 20)
    private String appVersion;
}
