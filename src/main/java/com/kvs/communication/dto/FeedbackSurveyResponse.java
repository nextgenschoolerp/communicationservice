package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackSurveyResponse {

    private UUID surveyId;
    private UUID tenantId;
    private String title;
    private String description;
    private String surveyType;
    private Boolean isActive;
    private Instant createdAt;
}
