package com.kvs.communication.repo;

import com.kvs.communication.entity.SchoolDocumentContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SchoolDocumentContentRepository extends JpaRepository<SchoolDocumentContent, UUID> {
}
