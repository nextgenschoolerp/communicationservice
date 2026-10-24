package com.kvs.communication.repo;

import com.kvs.communication.entity.VisitorAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VisitorAppointmentRepository extends JpaRepository<VisitorAppointment, UUID> {

    List<VisitorAppointment> findByTenantIdAndStatus(String tenantId, String status);

    List<VisitorAppointment> findByTenantIdOrderByAppointmentTimeDesc(String tenantId);
}
