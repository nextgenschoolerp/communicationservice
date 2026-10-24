package com.kvs.communication.repo;

import com.kvs.communication.entity.GrievanceTicket;
import com.kvs.communication.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GrievanceTicketRepository extends JpaRepository<GrievanceTicket, UUID> {

    List<GrievanceTicket> findByTenantIdAndStatus(String tenantId, TicketStatus status);

    List<GrievanceTicket> findByTenantIdAndParentUserIdOrderByCreatedAtDesc(String tenantId, UUID parentUserId);

    List<GrievanceTicket> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}
