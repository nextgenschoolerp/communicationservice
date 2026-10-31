package com.kvs.communication.dto;

import com.kvs.communication.enums.TicketStatus;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceUpdateRequest {

    private TicketStatus status;
    private String assignedToUserId;
    private String resolutionNotes;
}
