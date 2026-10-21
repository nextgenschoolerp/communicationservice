package com.kvs.communication.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlotResponse {

    private UUID slotId;
    private UUID tenantId;
    private UUID hostUserId;
    private String hostName;
    private LocalDate slotDate;
    private String startTime;
    private String endTime;
    private Boolean isBooked;
    private UUID bookedByParentId;
    private String bookedByParentName;
}
