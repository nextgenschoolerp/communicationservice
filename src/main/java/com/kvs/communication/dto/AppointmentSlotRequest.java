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
public class AppointmentSlotRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Host user ID is required")
    private UUID hostUserId;

    @NotBlank(message = "Host name is required")
    @Size(max = 150)
    private String hostName;

    @NotNull(message = "Slot date is required")
    private LocalDate slotDate;

    @NotBlank(message = "Start time is required (HH:mm)")
    @Size(max = 10)
    private String startTime;

    @NotBlank(message = "End time is required (HH:mm)")
    @Size(max = 10)
    private String endTime;
}
