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
public class ParentChildMappingRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "Parent user ID is required")
    private String parentUserId;

    @NotBlank(message = "Parent phone is required")
    @Size(max = 20)
    private String parentPhone;

    @NotBlank(message = "Parent name is required")
    @Size(max = 150)
    private String parentName;

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

    private Boolean isPrimary;

    @Size(max = 30)
    private String relationship;
}
