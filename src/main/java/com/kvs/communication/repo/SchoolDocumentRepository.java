package com.kvs.communication.repo;

import com.kvs.communication.entity.SchoolDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchoolDocumentRepository extends JpaRepository<SchoolDocument, UUID> {

    List<SchoolDocument> findByTenantIdOrderByModifiedOnDescFileNameAsc(UUID tenantId);

    List<SchoolDocument> findByTenantIdAndFolderIdOrderByModifiedOnDescFileNameAsc(UUID tenantId, UUID folderId);
}
