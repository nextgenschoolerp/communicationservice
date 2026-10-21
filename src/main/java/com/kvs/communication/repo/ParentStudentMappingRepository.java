package com.kvs.communication.repo;

import com.kvs.communication.entity.ParentStudentMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParentStudentMappingRepository extends JpaRepository<ParentStudentMapping, UUID> {

    List<ParentStudentMapping> findByParentUserId(UUID parentUserId);

    Optional<ParentStudentMapping> findByParentUserIdAndStudentId(UUID parentUserId, UUID studentId);
}
