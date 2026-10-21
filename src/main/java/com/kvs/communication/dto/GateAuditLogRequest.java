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
public class GateAuditLogRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotBlank(message = "Gate ID is required")
    @Size(max = 50)
    private String gateId;

    @NotBlank(message = "Gate name is required")
    @Size(max = 100)
    private String gateName;

    private UUID passId;

    @NotBlank(message = "Pass type is required (e.g. STUDENT_GATE_PASS, VISITOR_PASS)")
    @Size(max = 30)
    private String passType;

    @NotBlank(message = "Person name is required")
    @Size(max = 150)
    private String personName;

    @NotBlank(message = "Event type is required (ENTRY, EXIT, UNAUTHORIZED_ATTEMPT)")
    @Size(max = 30)
    private String eventType;

    @NotBlank(message = "Outcome is required (GRANTED, DENIED)")
    @Size(max = 20)
    private String outcome;

    private String notes;
}
