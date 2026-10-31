package com.kvs.communication.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveySubmissionRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "Survey ID is required")
    private UUID surveyId;

    @NotNull(message = "Parent user ID is required")
    private String parentUserId;

    @NotBlank(message = "Parent name is required")
    @Size(max = 150)
    private String parentName;

    @NotNull(message = "Rating score is required (1 to 5)")
    @Min(value = 1, message = "Rating score minimum is 1")
    @Max(value = 5, message = "Rating score maximum is 5")
    private Integer ratingScore;

    private String comments;
}
