package com.kvs.communication.repo;

import com.kvs.communication.entity.StudentGatePass;
import com.kvs.communication.enums.GatePassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentGatePassRepository extends JpaRepository<StudentGatePass, UUID> {

    Optional<StudentGatePass> findByQrCode(String qrCode);

    List<StudentGatePass> findByTenantIdAndStudentId(UUID tenantId, UUID studentId);

    List<StudentGatePass> findByTenantIdAndStatus(UUID tenantId, GatePassStatus status);
}
