package com.kvs.communication.dto;

import com.kvs.communication.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceTicketRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Parent user ID is required")
    private UUID parentUserId;

    @NotBlank(message = "Parent name is required")
    @Size(max = 150)
    private String parentName;

    private UUID studentId;

    @Size(max = 150)
    private String studentName;

    @NotBlank(message = "Category is required")
    @Size(max = 50)
    private String category;

    private TicketPriority priority;

    @NotBlank(message = "Subject is required")
    @Size(max = 200)
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    private Integer slaDueHours;
}
