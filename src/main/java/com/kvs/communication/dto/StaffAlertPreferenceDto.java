package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/** Settings → Notifications: request and response (a null switch is left as it is). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAlertPreferenceDto {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotBlank(message = "User ID is required")
    @Size(max = 100)
    private String userId;

    private Boolean attendanceAlerts;
    private Boolean feeReminders;
    private Boolean leaveRequests;
    private Boolean dailyDigest;
}
