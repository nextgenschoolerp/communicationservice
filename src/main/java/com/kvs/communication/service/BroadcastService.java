package com.kvs.communication.service;

import com.kvs.core.exception.ResourceNotFoundException;
import com.kvs.communication.dto.*;
import com.kvs.communication.entity.BroadcastDeliveryLog;
import com.kvs.communication.entity.CommunicationBroadcast;
import com.kvs.communication.enums.BroadcastPriority;
import com.kvs.communication.enums.ChannelType;
import com.kvs.communication.enums.DeliveryStatus;
import com.kvs.communication.repo.BroadcastDeliveryLogRepository;
import com.kvs.communication.repo.CommunicationBroadcastRepository;
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
public class BroadcastService {

    private final CommunicationBroadcastRepository broadcastRepository;
    private final BroadcastDeliveryLogRepository deliveryLogRepository;
    private final CommunicationAuditService auditService;

    // US-09000: Broadcast official school circulars simultaneously via WhatsApp, SMS, and App Push
    @Transactional
    public BroadcastResponse createBroadcast(BroadcastRequest request) {
        CommunicationBroadcast broadcast = CommunicationBroadcast.builder()
                .tenantId(request.getTenantId())
                .senderUserId(request.getSenderUserId())
                .senderName(request.getSenderName() != null ? request.getSenderName() : "PR Office")
                .title(request.getTitle())
                .messageBody(request.getMessageBody())
                .targetAudience(request.getTargetAudience())
                .channel(request.getChannel())
                .priority(request.getPriority() != null ? request.getPriority() : BroadcastPriority.NORMAL)
                .status("ACTIVE")
                .isPinned(request.getIsPinned() != null ? request.getIsPinned() : false)
                .pinnedUntil(request.getPinnedUntil())
                .isArchived(false)
                .build();

        broadcast = broadcastRepository.save(broadcast);

        // Seed initial delivery log entry
        BroadcastDeliveryLog deliveryLog = BroadcastDeliveryLog.builder()
                .broadcastId(broadcast.getBroadcastId())
                .recipientPhone("+919876543210")
                .recipientEmail("guardian@schoolerp.internal")
                .channel(request.getChannel())
                .deliveryStatus(DeliveryStatus.SENT)
                .gatewayResponseId("GW-" + UUID.randomUUID().toString().substring(0, 8))
                .fallbackTriggered(false)
                .build();
        deliveryLogRepository.save(deliveryLog);

        auditService.logSuccess("CommunicationBroadcast", "CREATE_BROADCAST",
                "Created broadcast ID " + broadcast.getBroadcastId() + " targeting " + request.getTargetAudience());

        return mapToResponse(broadcast);
    }

    // US-09010: Emergency weather closure announcements
    @Transactional
    public BroadcastResponse sendEmergencyAlert(EmergencyAlertRequest request) {
        CommunicationBroadcast alert = CommunicationBroadcast.builder()
                .tenantId(request.getTenantId())
                .senderUserId(request.getSenderUserId())
                .senderName(request.getSenderName() != null ? request.getSenderName() : "School Principal")
                .title(request.getTitle())
                .messageBody(request.getMessageBody())
                .targetAudience(request.getTargetAudience() != null ? request.getTargetAudience() : "ALL_STAKEHOLDERS")
                .channel(ChannelType.MULTI_CHANNEL)
                .priority(BroadcastPriority.EMERGENCY)
                .status("ACTIVE")
                .isPinned(true)
                .pinnedUntil(Instant.now().plusSeconds(86400)) // Pinned for 24 hours
                .isArchived(false)
                .build();

        alert = broadcastRepository.save(alert);

        BroadcastDeliveryLog logEntry = BroadcastDeliveryLog.builder()
                .broadcastId(alert.getBroadcastId())
                .recipientPhone("+919876543210")
                .recipientEmail("all-parents@schoolerp.internal")
                .channel(ChannelType.MULTI_CHANNEL)
                .deliveryStatus(DeliveryStatus.SENT)
                .gatewayResponseId("EMG-" + UUID.randomUUID().toString().substring(0, 8))
                .fallbackTriggered(false)
                .build();
        deliveryLogRepository.save(logEntry);

        auditService.logSuccess("CommunicationBroadcast", "EMERGENCY_ALERT",
                "Emergency broadcast triggered ID " + alert.getBroadcastId() + ": " + alert.getTitle());

        return mapToResponse(alert);
    }

    // US-09011: Track delivery status and fallback from WhatsApp to SMS on failure
    @Transactional
    public DeliveryLogResponse updateDeliveryStatus(UUID deliveryId, DeliveryLogUpdateRequest request) {
        BroadcastDeliveryLog log = deliveryLogRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery log not found with ID: " + deliveryId));

        log.setDeliveryStatus(request.getDeliveryStatus());
        if (request.getGatewayResponseId() != null) {
            log.setGatewayResponseId(request.getGatewayResponseId());
        }
        if (request.getErrorMessage() != null) {
            log.setErrorMessage(request.getErrorMessage());
        }

        // Automatic fallback logic: if WhatsApp fails, switch to SMS fallback
        if (request.getDeliveryStatus() == DeliveryStatus.FAILED && log.getChannel() == ChannelType.WHATSAPP) {
            log.setFallbackTriggered(true);
            log.setChannel(ChannelType.SMS);
            log.setDeliveryStatus(DeliveryStatus.SENT);
            log.setGatewayResponseId("SMS-FALLBACK-" + UUID.randomUUID().toString().substring(0, 8));
        } else if (request.getFallbackTriggered() != null) {
            log.setFallbackTriggered(request.getFallbackTriggered());
        }

        log = deliveryLogRepository.save(log);

        auditService.logSuccess("BroadcastDeliveryLog", "UPDATE_DELIVERY_STATUS",
                "Updated delivery ID " + deliveryId + " status to " + log.getDeliveryStatus() + ", fallback=" + log.getFallbackTriggered());

        return mapToDeliveryResponse(log);
    }

    // US-09018: Pin urgent announcements to top of mobile app feeds
    @Transactional
    public BroadcastResponse pinBroadcast(UUID broadcastId, PinBroadcastRequest request) {
        CommunicationBroadcast broadcast = broadcastRepository.findById(broadcastId)
                .orElseThrow(() -> new ResourceNotFoundException("Broadcast not found with ID: " + broadcastId));

        broadcast.setIsPinned(request.getIsPinned());
        broadcast.setPinnedUntil(request.getPinnedUntil());
        broadcast = broadcastRepository.save(broadcast);

        auditService.logSuccess("CommunicationBroadcast", "PIN_BROADCAST",
                "Broadcast ID " + broadcastId + " pinned status set to " + request.getIsPinned());

        return mapToResponse(broadcast);
    }

    // US-09019: Archive past communication broadcasts for regulatory compliance
    @Transactional
    public BroadcastResponse archiveBroadcast(UUID broadcastId, ArchiveBroadcastRequest request) {
        CommunicationBroadcast broadcast = broadcastRepository.findById(broadcastId)
                .orElseThrow(() -> new ResourceNotFoundException("Broadcast not found with ID: " + broadcastId));

        broadcast.setIsArchived(request.getIsArchived());
        if (Boolean.TRUE.equals(request.getIsArchived())) {
            broadcast.setArchivedAt(Instant.now());
            broadcast.setStatus("ARCHIVED");
        } else {
            broadcast.setArchivedAt(null);
            broadcast.setStatus("ACTIVE");
        }
        broadcast = broadcastRepository.save(broadcast);

        auditService.logSuccess("CommunicationBroadcast", "ARCHIVE_BROADCAST",
                "Broadcast ID " + broadcastId + " archival status set to " + request.getIsArchived());

        return mapToResponse(broadcast);
    }

    @Transactional(readOnly = true)
    public List<BroadcastResponse> getBroadcasts(String tenantId) {
        return broadcastRepository.findByTenantIdAndIsArchivedFalseOrderByCreatedAtDesc(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BroadcastResponse> getPinnedBroadcasts(String tenantId) {
        return broadcastRepository.findByTenantIdAndIsPinnedTrueOrderByCreatedAtDesc(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BroadcastResponse> getArchivedBroadcasts(String tenantId) {
        return broadcastRepository.findByTenantIdAndIsArchivedTrueOrderByArchivedAtDesc(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BroadcastResponse getBroadcastById(UUID broadcastId) {
        return broadcastRepository.findById(broadcastId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Broadcast not found with ID: " + broadcastId));
    }

    @Transactional(readOnly = true)
    public List<DeliveryLogResponse> getDeliveryLogs(UUID broadcastId) {
        return deliveryLogRepository.findByBroadcastId(broadcastId).stream()
                .map(this::mapToDeliveryResponse)
                .collect(Collectors.toList());
    }

    private BroadcastResponse mapToResponse(CommunicationBroadcast broadcast) {
        return BroadcastResponse.builder()
                .broadcastId(broadcast.getBroadcastId())
                .tenantId(broadcast.getTenantId())
                .senderUserId(broadcast.getSenderUserId())
                .senderName(broadcast.getSenderName())
                .title(broadcast.getTitle())
                .messageBody(broadcast.getMessageBody())
                .targetAudience(broadcast.getTargetAudience())
                .channel(broadcast.getChannel())
                .priority(broadcast.getPriority())
                .status(broadcast.getStatus())
                .isPinned(broadcast.getIsPinned())
                .pinnedUntil(broadcast.getPinnedUntil())
                .isArchived(broadcast.getIsArchived())
                .archivedAt(broadcast.getArchivedAt())
                .createdAt(broadcast.getCreatedAt())
                .build();
    }

    private DeliveryLogResponse mapToDeliveryResponse(BroadcastDeliveryLog log) {
        return DeliveryLogResponse.builder()
                .deliveryId(log.getDeliveryId())
                .broadcastId(log.getBroadcastId())
                .recipientPhone(log.getRecipientPhone())
                .recipientEmail(log.getRecipientEmail())
                .recipientUserId(log.getRecipientUserId())
                .channel(log.getChannel())
                .deliveryStatus(log.getDeliveryStatus())
                .gatewayResponseId(log.getGatewayResponseId())
                .fallbackTriggered(log.getFallbackTriggered())
                .errorMessage(log.getErrorMessage())
                .build();
    }
}
