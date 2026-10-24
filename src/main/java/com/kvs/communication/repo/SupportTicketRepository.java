package com.kvs.communication.repo;

import com.kvs.communication.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {

    List<SupportTicket> findByTenantIdOrderByLastActivityAtDesc(String tenantId);

    List<SupportTicket> findByTenantIdAndRequesterUserIdOrderByLastActivityAtDesc(String tenantId, String requesterUserId);

    /** Highest number used so far ("SUP-1048" -> 1048); numbers are shared by every school */
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(ticket_number FROM 5) AS INTEGER)), 1000) FROM support_tickets", nativeQuery = true)
    int maxTicketNumber();
}
