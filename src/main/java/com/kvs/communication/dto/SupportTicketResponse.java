package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketResponse {

    private UUID ticketId;
    private String ticketNumber;
    private String subject;
    private String category;
    private String priority;
    private String status;
    private String requesterUserId;
    private String requesterName;
    private Instant createdAt;
    private Instant lastActivityAt;
    private Instant resolvedAt;
    /** Only filled when one ticket is read */
    private List<SupportMessageResponse> messages;
}
