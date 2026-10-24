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
public class GatePassRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotBlank(message = "Student name is required")
    @Size(max = 150)
    private String studentName;

    @NotBlank(message = "Grade is required")
    @Size(max = 20)
    private String grade;

    @NotBlank(message = "Section is required")
    @Size(max = 10)
    private String section;

    private UUID guardianId;

    @NotBlank(message = "Guardian name is required")
    @Size(max = 150)
    private String guardianName;

    @NotBlank(message = "Pickup person name is required")
    @Size(max = 150)
    private String pickupPersonName;

    @NotBlank(message = "Pickup person phone is required")
    @Size(max = 20)
    private String pickupPersonPhone;

    @NotBlank(message = "Reason is required")
    private String reason;
}
