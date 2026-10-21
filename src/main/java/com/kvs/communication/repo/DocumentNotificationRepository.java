package com.kvs.communication.repo;

import com.kvs.communication.entity.DocumentNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentNotificationRepository extends JpaRepository<DocumentNotification, UUID> {

    List<DocumentNotification> findByTenantIdAndStudentId(UUID tenantId, UUID studentId);

    List<DocumentNotification> findByTenantIdAndDocumentType(UUID tenantId, String documentType);
}
