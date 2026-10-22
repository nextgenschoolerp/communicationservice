package com.kvs.communication.repo;

import com.kvs.communication.entity.DocumentFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentFolderRepository extends JpaRepository<DocumentFolder, UUID> {

    List<DocumentFolder> findByTenantIdOrderBySortOrderAscNameAsc(UUID tenantId);

    boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);
}
