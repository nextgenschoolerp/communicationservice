package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorAppointmentRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Visitor name is required")
    @Size(max = 150)
    private String visitorName;

    @NotBlank(message = "Visitor phone is required")
    @Size(max = 20)
    private String visitorPhone;

    @Size(max = 50)
    private String visitorIdType;

    @Size(max = 50)
    private String visitorIdNumber;

    @NotBlank(message = "Purpose is required")
    private String purpose;

    private String hostStaffId;

    @Size(max = 150)
    private String hostStaffName;

    private Instant appointmentTime;
}
