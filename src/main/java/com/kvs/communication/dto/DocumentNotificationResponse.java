package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentNotificationResponse {

    private UUID notificationId;
    private UUID tenantId;
    private String documentType;
    private String documentId;
    private UUID studentId;
    private String parentPhone;
    private String parentEmail;
    private String downloadUrl;
    private String deliveryStatus;
    private Instant sentAt;
}
