package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GateAuditLogResponse {

    private UUID auditId;
    private String tenantId;
    private String gateId;
    private String gateName;
    private UUID passId;
    private String passType;
    private String personName;
    private String eventType;
    private String outcome;
    private String notes;
    private Instant timestamp;
}
