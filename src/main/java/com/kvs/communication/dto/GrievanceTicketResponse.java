package com.kvs.communication.dto;

import com.kvs.communication.enums.TicketPriority;
import com.kvs.communication.enums.TicketStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceTicketResponse {

    private UUID ticketId;
    private String tenantId;
    private String parentUserId;
    private String parentName;
    private UUID studentId;
    private String studentName;
    private String category;
    private TicketPriority priority;
    private String subject;
    private String description;
    private TicketStatus status;
    private Integer slaDueHours;
    private String assignedToUserId;
    private String resolutionNotes;
    private Instant createdAt;
    private Instant resolvedAt;
}
