package com.kvs.communication.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveBroadcastRequest {

    @NotNull(message = "isArchived flag is required")
    private Boolean isArchived;
}
