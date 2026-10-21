package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatePassVerifyRequest {

    @NotBlank(message = "QR Code is required")
    private String qrCode;

    @NotBlank(message = "Security guard name is required")
    private String securityGuardName;

    private String gateId;
}
