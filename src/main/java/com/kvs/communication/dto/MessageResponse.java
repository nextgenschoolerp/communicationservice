package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private UUID messageId;
    private UUID tenantId;
    private UUID senderId;
    private String senderName;
    private String senderRole;
    private UUID recipientId;
    private String recipientName;
    private UUID studentId;
    private UUID threadId;
    private String subject;
    private String content;
    private Boolean isRead;
    private Instant readAt;
    private Instant createdAt;
}
