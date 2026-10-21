package com.kvs.communication.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinBroadcastRequest {

    @NotNull(message = "isPinned flag is required")
    private Boolean isPinned;

    private Instant pinnedUntil;
}
