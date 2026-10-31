package com.kvs.communication.dto;

import com.kvs.communication.enums.ChannelType;
import com.kvs.communication.enums.DeliveryStatus;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryLogResponse {

    private UUID deliveryId;
    private UUID broadcastId;
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private UUID recipientUserId;
    private ChannelType channel;
    private DeliveryStatus deliveryStatus;
    private String gatewayResponseId;
    private Boolean fallbackTriggered;
    private String errorMessage;
}
