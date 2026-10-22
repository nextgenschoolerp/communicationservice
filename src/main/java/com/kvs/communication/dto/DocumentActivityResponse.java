package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentActivityResponse {

    private UUID logId;
    private String actorName;
    private String action;
    private Instant occurredAt;
}
