package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorAppointmentResponse {

    private UUID appointmentId;
    private UUID tenantId;
    private String visitorName;
    private String visitorPhone;
    private String visitorIdType;
    private String visitorIdNumber;
    private String purpose;
    private UUID hostStaffId;
    private String hostStaffName;
    private Instant appointmentTime;
    private Instant checkInTime;
    private Instant checkOutTime;
    private String badgeNumber;
    private String status;
    private Instant createdAt;
}
