package com.kvs.communication.dto;

import com.kvs.communication.enums.DeliveryStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceAlertResponse {

    private UUID alertId;
    private UUID tenantId;
    private UUID studentId;
    private String studentName;
    private String guardianPhone;
    private String guardianName;
    private LocalDate absenceDate;
    private String alertChannel;
    private DeliveryStatus deliveryStatus;
    private Boolean escalated;
    private Instant escalatedAt;
    private Instant createdAt;
}
