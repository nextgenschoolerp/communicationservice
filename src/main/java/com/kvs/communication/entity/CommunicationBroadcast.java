package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import com.kvs.communication.enums.BroadcastPriority;
import com.kvs.communication.enums.ChannelType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "communication_broadcasts", indexes = {
        @Index(name = "idx_comm_broadcast_tenant", columnList = "tenant_id"),
        @Index(name = "idx_comm_broadcast_status", columnList = "status"),
        @Index(name = "idx_comm_broadcast_priority", columnList = "priority")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunicationBroadcast extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "broadcast_id", updatable = false, nullable = false)
    private UUID broadcastId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "sender_user_id", nullable = false)
    private UUID senderUserId;

    @Column(name = "sender_name", length = 150)
    private String senderName;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    @Column(name = "target_audience", nullable = false, length = 50)
    private String targetAudience;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private ChannelType channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    @Builder.Default
    private BroadcastPriority priority = BroadcastPriority.NORMAL;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "is_pinned")
    @Builder.Default
    private Boolean isPinned = false;

    @Column(name = "pinned_until")
    private Instant pinnedUntil;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean isArchived = false;

    @Column(name = "archived_at")
    private Instant archivedAt;
}
