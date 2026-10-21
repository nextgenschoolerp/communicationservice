package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "survey_responses", indexes = {
        @Index(name = "idx_survey_resp_survey", columnList = "survey_id"),
        @Index(name = "idx_survey_resp_parent", columnList = "parent_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResponse extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "response_id", updatable = false, nullable = false)
    private UUID responseId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "survey_id", nullable = false)
    private UUID surveyId;

    @Column(name = "parent_user_id", nullable = false)
    private UUID parentUserId;

    @Column(name = "parent_name", nullable = false, length = 150)
    private String parentName;

    @Column(name = "rating_score", nullable = false)
    private Integer ratingScore; // 1 to 5

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
}
