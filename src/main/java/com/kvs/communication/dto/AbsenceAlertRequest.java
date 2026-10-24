package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceAlertRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotBlank(message = "Student name is required")
    @Size(max = 150)
    private String studentName;

    @NotBlank(message = "Guardian phone is required")
    @Size(max = 20)
    private String guardianPhone;

    @Size(max = 150)
    private String guardianName;

    private LocalDate absenceDate;

    private String alertChannel;

    private String grade;

    private String section;
}
