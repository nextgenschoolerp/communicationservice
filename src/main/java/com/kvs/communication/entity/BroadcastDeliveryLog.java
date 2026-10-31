package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import com.kvs.communication.enums.ChannelType;
import com.kvs.communication.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "broadcast_delivery_logs", indexes = {
        @Index(name = "idx_delivery_broadcast_status", columnList = "broadcast_id, delivery_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastDeliveryLog extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id", updatable = false, nullable = false)
    private UUID deliveryId;

    @Column(name = "broadcast_id", nullable = false)
    private UUID broadcastId;

    /** "Rohit Sharma · Father of Aarav Sharma (Class 7)" */
    @Column(name = "recipient_name", length = 300)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "recipient_email", length = 100)
    private String recipientEmail;

    @Column(name = "recipient_user_id")
    private UUID recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private ChannelType channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", length = 20)
    @Builder.Default
    private DeliveryStatus deliveryStatus = DeliveryStatus.SENT;

    @Column(name = "gateway_response_id", length = 100)
    private String gatewayResponseId;

    @Column(name = "fallback_triggered")
    @Builder.Default
    private Boolean fallbackTriggered = false;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
