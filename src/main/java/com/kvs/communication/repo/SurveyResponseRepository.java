package com.kvs.communication.repo;

import com.kvs.communication.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, UUID> {

    List<SurveyResponse> findBySurveyId(UUID surveyId);

    List<SurveyResponse> findByTenantIdAndParentUserId(String tenantId, UUID parentUserId);
}
