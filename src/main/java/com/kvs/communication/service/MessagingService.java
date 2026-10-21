package com.kvs.communication.service;

import com.kvs.core.exception.ResourceNotFoundException;
import com.kvs.communication.dto.MessageResponse;
import com.kvs.communication.dto.SendMessageRequest;
import com.kvs.communication.entity.ConfidentialMessage;
import com.kvs.communication.repo.ConfidentialMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagingService {

    private final ConfidentialMessageRepository messageRepository;
    private final CommunicationAuditService auditService;

    // US-09004: Facilitate secure two-way messaging between parents and class teachers
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        UUID threadId = request.getThreadId() != null ? request.getThreadId() : UUID.randomUUID();

        ConfidentialMessage message = ConfidentialMessage.builder()
                .tenantId(request.getTenantId())
                .senderId(request.getSenderId())
                .senderName(request.getSenderName())
                .senderRole(request.getSenderRole())
                .recipientId(request.getRecipientId())
                .recipientName(request.getRecipientName())
                .studentId(request.getStudentId())
                .threadId(threadId)
                .subject(request.getSubject())
                .content(request.getContent())
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        auditService.logSuccess("ConfidentialMessage", "SEND_MESSAGE",
                "Sent message from " + message.getSenderName() + " to " + message.getRecipientName() + " (thread: " + threadId + ")");

        return mapToResponse(message);
    }

    @Transactional
    public MessageResponse markMessageRead(UUID messageId) {
        ConfidentialMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        message.setIsRead(true);
        message.setReadAt(Instant.now());
        message = messageRepository.save(message);

        return mapToResponse(message);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getThreadMessages(UUID threadId) {
        return messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getUserInbox(UUID recipientId) {
        return messageRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private MessageResponse mapToResponse(ConfidentialMessage m) {
        return MessageResponse.builder()
                .messageId(m.getMessageId())
                .tenantId(m.getTenantId())
                .senderId(m.getSenderId())
                .senderName(m.getSenderName())
                .senderRole(m.getSenderRole())
                .recipientId(m.getRecipientId())
                .recipientName(m.getRecipientName())
                .studentId(m.getStudentId())
                .threadId(m.getThreadId())
                .subject(m.getSubject())
                .content(m.getContent())
                .isRead(m.getIsRead())
                .readAt(m.getReadAt())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
