package com.kvs.communication.repo;

import com.kvs.communication.entity.CommunicationBroadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunicationBroadcastRepository extends JpaRepository<CommunicationBroadcast, UUID> {

    List<CommunicationBroadcast> findByTenantIdAndIsArchivedFalseOrderByCreatedAtDesc(UUID tenantId);

    List<CommunicationBroadcast> findByTenantIdAndIsPinnedTrueOrderByCreatedAtDesc(UUID tenantId);

    List<CommunicationBroadcast> findByTenantIdAndIsArchivedTrueOrderByArchivedAtDesc(UUID tenantId);

    List<CommunicationBroadcast> findByTenantIdAndTargetAudience(UUID tenantId, String targetAudience);
}
