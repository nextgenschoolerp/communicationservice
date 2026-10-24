package com.kvs.communication.repo;

import com.kvs.communication.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {

    List<CalendarEvent> findByTenantIdAndIsPublishedTrueOrderByStartDateAsc(String tenantId);

    List<CalendarEvent> findByTenantIdAndStartDateBetween(String tenantId, LocalDate start, LocalDate end);
}
