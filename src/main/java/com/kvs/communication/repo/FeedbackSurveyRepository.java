package com.kvs.communication.repo;

import com.kvs.communication.entity.FeedbackSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackSurveyRepository extends JpaRepository<FeedbackSurvey, UUID> {

    List<FeedbackSurvey> findByTenantIdAndIsActiveTrue(UUID tenantId);
}
