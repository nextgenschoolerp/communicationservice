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
public class DocumentNotificationRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotBlank(message = "Document type is required (e.g., FEE_RECEIPT, REPORT_CARD)")
    @Size(max = 50)
    private String documentType;

    @NotBlank(message = "Document ID is required")
    @Size(max = 100)
    private String documentId;

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotBlank(message = "Parent phone is required")
    @Size(max = 20)
    private String parentPhone;

    @Size(max = 100)
    private String parentEmail;

    @NotBlank(message = "Download URL is required")
    @Size(max = 500)
    private String downloadUrl;
}
