package com.kvs.communication.dto;

import com.kvs.communication.enums.GatePassStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatePassResponse {

    private UUID passId;
    private UUID tenantId;
    private UUID studentId;
    private String studentName;
    private String grade;
    private String section;
    private UUID guardianId;
    private String guardianName;
    private String pickupPersonName;
    private String pickupPersonPhone;
    private String reason;
    private String qrCode;
    private GatePassStatus status;
    private Boolean guardianApproved;
    private String securityVerifiedBy;
    private Instant verifiedAt;
    private Instant exitTime;
    private Instant createdAt;
}
