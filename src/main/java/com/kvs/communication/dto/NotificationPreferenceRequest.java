package com.kvs.communication.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    private Boolean whatsappEnabled;
    private Boolean smsEnabled;
    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private String quietHoursStart;
    private String quietHoursEnd;
}
