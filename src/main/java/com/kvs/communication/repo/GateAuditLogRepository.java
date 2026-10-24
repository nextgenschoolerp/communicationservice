package com.kvs.communication.repo;

import com.kvs.communication.entity.GateAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GateAuditLogRepository extends JpaRepository<GateAuditLog, UUID> {

    List<GateAuditLog> findByTenantIdOrderByTimestampDesc(String tenantId);

    List<GateAuditLog> findByTenantIdAndEventType(String tenantId, String eventType);
}
