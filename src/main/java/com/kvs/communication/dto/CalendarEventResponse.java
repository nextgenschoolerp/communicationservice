package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventResponse {

    private UUID eventId;
    private UUID tenantId;
    private String title;
    private String description;
    private String eventType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startTime;
    private String endTime;
    private String venue;
    private String organiser;
    private String targetAudience;
    private String mediaUrls;
    private Boolean isPublished;
    private Instant createdAt;
}
