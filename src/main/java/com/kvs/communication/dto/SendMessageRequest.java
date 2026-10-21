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
public class SendMessageRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Sender ID is required")
    private UUID senderId;

    @NotBlank(message = "Sender name is required")
    @Size(max = 150)
    private String senderName;

    @NotBlank(message = "Sender role is required")
    @Size(max = 50)
    private String senderRole;

    @NotNull(message = "Recipient ID is required")
    private UUID recipientId;

    @NotBlank(message = "Recipient name is required")
    @Size(max = 150)
    private String recipientName;

    private UUID studentId;

    private UUID threadId;

    @NotBlank(message = "Subject is required")
    @Size(max = 200)
    private String subject;

    @NotBlank(message = "Content is required")
    private String content;
}
