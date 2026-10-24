package com.kvs.communication.dto;

import com.kvs.communication.enums.BroadcastPriority;
import com.kvs.communication.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    private String senderUserId;
    private String senderId;

    public String getSenderUserId() {
        return senderUserId != null && !senderUserId.isBlank() ? senderUserId : senderId;
    }

    @Size(max = 150)
    private String senderName;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Message body is required")
    private String messageBody;

    @NotBlank(message = "Target audience is required")
    @Size(max = 50)
    private String targetAudience;

    @NotNull(message = "Channel is required")
    private ChannelType channel;

    private BroadcastPriority priority;

    private Boolean isPinned;

    private Instant pinnedUntil;
}
