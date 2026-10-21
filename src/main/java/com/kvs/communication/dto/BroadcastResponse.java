package com.kvs.communication.dto;

import com.kvs.communication.enums.BroadcastPriority;
import com.kvs.communication.enums.ChannelType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastResponse {

    private UUID broadcastId;
    private UUID tenantId;
    private UUID senderUserId;
    private String senderName;
    private String title;
    private String messageBody;
    private String targetAudience;
    private ChannelType channel;
    private BroadcastPriority priority;
    private String status;
    private Boolean isPinned;
    private Instant pinnedUntil;
    private Boolean isArchived;
    private Instant archivedAt;
    private Instant createdAt;
}
