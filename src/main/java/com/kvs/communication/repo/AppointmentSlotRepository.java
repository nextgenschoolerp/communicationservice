package com.kvs.communication.repo;

import com.kvs.communication.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, UUID> {

    List<AppointmentSlot> findByTenantIdAndSlotDateOrderByStartTimeAsc(UUID tenantId, LocalDate slotDate);

    List<AppointmentSlot> findByTenantIdAndHostUserId(UUID tenantId, UUID hostUserId);
}
