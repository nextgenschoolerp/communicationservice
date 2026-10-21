package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAlertRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Sender user ID is required")
    private UUID senderUserId;

    private String senderName;

    @NotBlank(message = "Alert headline is required")
    private String title;

    @NotBlank(message = "Alert body is required")
    private String messageBody;

    private String targetAudience;
}
