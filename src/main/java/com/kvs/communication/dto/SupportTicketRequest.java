package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Requester is required")
    @Size(max = 100)
    private String requesterUserId;

    @NotBlank(message = "Requester name is required")
    @Size(max = 150)
    private String requesterName;

    @Size(max = 150)
    private String requesterEmail;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "ACCOUNT_ACCESS|STUDENTS|ATTENDANCE|FEES|ACADEMICS|OTHER", message = "Unknown category")
    private String category;

    @Pattern(regexp = "NORMAL|URGENT", message = "Priority must be NORMAL or URGENT")
    private String priority;

    @NotBlank(message = "Describe what you need help with")
    @Size(max = 4000)
    private String description;
}
