package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "feedback_surveys", indexes = {
        @Index(name = "idx_survey_tenant", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackSurvey extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "survey_id", updatable = false, nullable = false)
    private UUID surveyId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "survey_type", length = 50)
    @Builder.Default
    private String surveyType = "ACADEMIC";

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
