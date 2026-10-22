package com.kvs.communication.service;

import com.kvs.core.exception.ResourceNotFoundException;
import com.kvs.communication.dto.*;
import com.kvs.communication.entity.SupportTicket;
import com.kvs.communication.entity.SupportTicketMessage;
import com.kvs.communication.repo.SupportTicketMessageRepository;
import com.kvs.communication.repo.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Help & support: tickets raised by school staff and the conversation with the support team. */
@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final SupportTicketMessageRepository messageRepository;
    private final CommunicationAuditService auditService;

    @Transactional(readOnly = true)
    public List<SupportTicketResponse> list(UUID tenantId, String requesterUserId) {
        List<SupportTicket> tickets = requesterUserId == null || requesterUserId.isBlank()
                ? ticketRepository.findByTenantIdOrderByLastActivityAtDesc(tenantId)
                : ticketRepository.findByTenantIdAndRequesterUserIdOrderByLastActivityAtDesc(tenantId, requesterUserId);
        return tickets.stream().map(t -> map(t, null)).collect(Collectors.toList());
    }

    @Transactional
    public SupportTicketResponse create(SupportTicketRequest request) {
        Instant now = Instant.now();
        SupportTicket ticket = ticketRepository.save(SupportTicket.builder()
                .tenantId(request.getTenantId())
                .ticketNumber("SUP-" + (ticketRepository.maxTicketNumber() + 1))
                .subject(request.getSubject().trim())
                .category(request.getCategory())
                .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                .requesterUserId(request.getRequesterUserId())
                .requesterName(request.getRequesterName())
                .requesterEmail(request.getRequesterEmail())
                .lastActivityAt(now)
                .build());
        messageRepository.save(SupportTicketMessage.builder()
                .ticketId(ticket.getTicketId())
                .authorName(request.getRequesterName())
                .authorRole("REQUESTER")
                .body(request.getDescription().trim())
                .sentAt(now)
                .build());
        auditService.logSuccess("SupportTicket", "CREATE_TICKET", "Raised support ticket " + ticket.getTicketNumber());
        return get(ticket.getTicketId());
    }

    @Transactional(readOnly = true)
    public SupportTicketResponse get(UUID ticketId) {
        SupportTicket ticket = ticket(ticketId);
        return map(ticket, messageRepository.findByTicketIdOrderBySentAtAsc(ticketId));
    }

    /** A reply; a requester's reply reopens a resolved ticket, the support team's first reply starts work on it. */
    @Transactional
    public SupportTicketResponse reply(UUID ticketId, SupportMessageRequest request) {
        SupportTicket ticket = ticket(ticketId);
        String role = request.getAuthorRole() != null ? request.getAuthorRole() : "REQUESTER";
        Instant now = Instant.now();
        messageRepository.save(SupportTicketMessage.builder()
                .ticketId(ticketId)
                .authorName(request.getAuthorName())
                .authorRole(role)
                .body(request.getBody().trim())
                .sentAt(now)
                .build());
        if ("REQUESTER".equals(role) && "RESOLVED".equals(ticket.getStatus())) {
            ticket.setStatus("OPEN");
            ticket.setResolvedAt(null);
        } else if ("SUPPORT".equals(role) && "OPEN".equals(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
        }
        ticket.setLastActivityAt(now);
        return get(ticketId);
    }

    @Transactional
    public SupportTicketResponse changeStatus(UUID ticketId, SupportStatusRequest request) {
        SupportTicket ticket = ticket(ticketId);
        ticket.setStatus(request.getStatus());
        ticket.setResolvedAt("RESOLVED".equals(request.getStatus()) ? Instant.now() : null);
        ticket.setLastActivityAt(Instant.now());
        auditService.logSuccess("SupportTicket", "TICKET_STATUS", ticket.getTicketNumber() + " -> " + request.getStatus());
        return get(ticketId);
    }

    private SupportTicket ticket(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "ticketId", ticketId));
    }

    private SupportTicketResponse map(SupportTicket t, List<SupportTicketMessage> messages) {
        return SupportTicketResponse.builder()
                .ticketId(t.getTicketId())
                .ticketNumber(t.getTicketNumber())
                .subject(t.getSubject())
                .category(t.getCategory())
                .priority(t.getPriority())
                .status(t.getStatus())
                .requesterUserId(t.getRequesterUserId())
                .requesterName(t.getRequesterName())
                .createdAt(t.getCreatedAt())
                .lastActivityAt(t.getLastActivityAt())
                .resolvedAt(t.getResolvedAt())
                .messages(messages == null ? null : messages.stream().map(m -> SupportMessageResponse.builder()
                        .messageId(m.getMessageId())
                        .authorName(m.getAuthorName())
                        .authorRole(m.getAuthorRole())
                        .body(m.getBody())
                        .sentAt(m.getSentAt())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
