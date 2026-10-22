package com.kvs.communication.repo;

import com.kvs.communication.entity.DocumentAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentAccessLogRepository extends JpaRepository<DocumentAccessLog, UUID> {

    List<DocumentAccessLog> findByDocumentIdOrderByOccurredAtDesc(UUID documentId);
}
