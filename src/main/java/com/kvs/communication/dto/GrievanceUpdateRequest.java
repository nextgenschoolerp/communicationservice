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
    private UUID assignedToUserId;
    private String resolutionNotes;
}
