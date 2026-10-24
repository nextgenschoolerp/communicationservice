package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyBulletinResponse {

    private UUID bulletinId;
    private String tenantId;
    private String bulletinType;
    private String title;
    private String contentDetails;
    private LocalDate effectiveDate;
    private String mealType;
    private String busRouteNumber;
    private Instant createdAt;
}
