package com.kvs.communication.dto;

import com.kvs.communication.enums.ChannelType;
import com.kvs.communication.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryLogUpdateRequest {

    @NotNull(message = "Delivery status is required")
    private DeliveryStatus deliveryStatus;

    private String gatewayResponseId;

    private Boolean fallbackTriggered;

    private String errorMessage;
}
