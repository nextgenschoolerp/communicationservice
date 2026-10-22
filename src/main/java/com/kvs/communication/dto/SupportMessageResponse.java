package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportMessageResponse {

    private UUID messageId;
    private String authorName;
    private String authorRole;
    private String body;
    private Instant sentAt;
}
