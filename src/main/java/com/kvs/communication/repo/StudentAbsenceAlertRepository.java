package com.kvs.communication.repo;

import com.kvs.communication.entity.StudentAbsenceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface StudentAbsenceAlertRepository extends JpaRepository<StudentAbsenceAlert, UUID> {

    List<StudentAbsenceAlert> findByTenantId(String tenantId);

    List<StudentAbsenceAlert> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    List<StudentAbsenceAlert> findByTenantIdAndAbsenceDate(String tenantId, LocalDate date);

    List<StudentAbsenceAlert> findByTenantIdAndStudentId(String tenantId, UUID studentId);
}
