package com.kvs.communication.repo;

import com.kvs.communication.entity.StudentAbsenceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface StudentAbsenceAlertRepository extends JpaRepository<StudentAbsenceAlert, UUID> {

    List<StudentAbsenceAlert> findByTenantIdAndAbsenceDate(UUID tenantId, LocalDate date);

    List<StudentAbsenceAlert> findByTenantIdAndStudentId(UUID tenantId, UUID studentId);
}
