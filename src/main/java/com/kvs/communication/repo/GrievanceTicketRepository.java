package com.kvs.communication.repo;

import com.kvs.communication.entity.GrievanceTicket;
import com.kvs.communication.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GrievanceTicketRepository extends JpaRepository<GrievanceTicket, UUID> {

    List<GrievanceTicket> findByTenantIdAndStatus(UUID tenantId, TicketStatus status);

    List<GrievanceTicket> findByTenantIdAndParentUserIdOrderByCreatedAtDesc(UUID tenantId, UUID parentUserId);

    List<GrievanceTicket> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
