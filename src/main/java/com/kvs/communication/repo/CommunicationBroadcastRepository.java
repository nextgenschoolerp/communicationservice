package com.kvs.communication.repo;

import com.kvs.communication.entity.CommunicationBroadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunicationBroadcastRepository extends JpaRepository<CommunicationBroadcast, UUID> {

    List<CommunicationBroadcast> findByTenantIdAndIsArchivedFalseOrderByCreatedAtDesc(String tenantId);

    List<CommunicationBroadcast> findByTenantIdAndIsPinnedTrueOrderByCreatedAtDesc(String tenantId);

    List<CommunicationBroadcast> findByTenantIdAndIsArchivedTrueOrderByArchivedAtDesc(String tenantId);

    List<CommunicationBroadcast> findByTenantIdAndTargetAudience(String tenantId, String targetAudience);
}
