package com.kvs.communication.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponseDto {

    private UUID responseId;
    private String tenantId;
    private UUID surveyId;
    private UUID parentUserId;
    private String parentName;
    private Integer ratingScore;
    private String comments;
    private Instant submittedAt;
}
