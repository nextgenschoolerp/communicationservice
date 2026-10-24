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
    private String tenantId;

    private String senderUserId;
    private String senderId;

    public String getSenderUserId() {
        return senderUserId != null && !senderUserId.isBlank() ? senderUserId : senderId;
    }

    private String senderName;

    @NotBlank(message = "Alert headline is required")
    private String title;

    @NotBlank(message = "Alert body is required")
    private String messageBody;

    private String targetAudience;
}
