package com.kvs.communication.controller;

import com.kvs.core.dto.ApiResponse;
import com.kvs.communication.config.CommunicationRoles;
import com.kvs.communication.dto.*;
import com.kvs.communication.service.BroadcastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/communication/broadcasts")
@RequiredArgsConstructor
public class BroadcastController {

    private final BroadcastService broadcastService;

    // US-09000: Broadcast official school circulars simultaneously via WhatsApp, SMS, and App Push
    @PostMapping
    @PreAuthorize(CommunicationRoles.BROADCAST_MANAGE)
    public ResponseEntity<ApiResponse<BroadcastResponse>> createBroadcast(
            @Valid @RequestBody BroadcastRequest request) {
        BroadcastResponse data = broadcastService.createBroadcast(request);
        return ResponseEntity.ok(ApiResponse.<BroadcastResponse>builder()
                .success(true)
                .message("Broadcast circular dispatched successfully")
                .data(data)
                .build());
    }

    @GetMapping
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<BroadcastResponse>>> getBroadcasts(
            @RequestParam String tenantId) {
        List<BroadcastResponse> data = broadcastService.getBroadcasts(tenantId);
        return ResponseEntity.ok(ApiResponse.<List<BroadcastResponse>>builder()
                .success(true)
                .message("Broadcast circulars retrieved successfully")
                .data(data)
                .build());
    }

    /** Who a circular to this audience would reach, and which of the school's channels can send now */
    @GetMapping("/audience-preview")
    @PreAuthorize(CommunicationRoles.BROADCAST_MANAGE)
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> previewAudience(@RequestParam String tenantId, @RequestParam String audience,
                                                                                @RequestParam(required = false) com.kvs.communication.enums.ChannelType channel) {
        return ResponseEntity.ok(ApiResponse.success(broadcastService.previewAudience(tenantId, audience, channel)));
    }

    @GetMapping("/{broadcastId}")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<BroadcastResponse>> getBroadcastById(
            @PathVariable UUID broadcastId) {
        BroadcastResponse data = broadcastService.getBroadcastById(broadcastId);
        return ResponseEntity.ok(ApiResponse.<BroadcastResponse>builder()
                .success(true)
                .message("Broadcast details retrieved successfully")
                .data(data)
                .build());
    }

    // US-09010: Emergency weather closure announcements
    @PostMapping("/emergency")
    @PreAuthorize(CommunicationRoles.LEADERSHIP_OR_ADMIN)
    public ResponseEntity<ApiResponse<BroadcastResponse>> sendEmergencyAlert(
            @Valid @RequestBody EmergencyAlertRequest request) {
        BroadcastResponse data = broadcastService.sendEmergencyAlert(request);
        return ResponseEntity.ok(ApiResponse.<BroadcastResponse>builder()
                .success(true)
                .message("Emergency broadcast dispatched to all stakeholders")
                .data(data)
                .build());
    }

    // US-09011: Track delivery status and trigger SMS fallback on failure
    @PutMapping("/delivery/{deliveryId}")
    @PreAuthorize(CommunicationRoles.SYSTEM_OR_ADMIN)
    public ResponseEntity<ApiResponse<DeliveryLogResponse>> updateDeliveryStatus(
            @PathVariable UUID deliveryId,
            @Valid @RequestBody DeliveryLogUpdateRequest request) {
        DeliveryLogResponse data = broadcastService.updateDeliveryStatus(deliveryId, request);
        return ResponseEntity.ok(ApiResponse.<DeliveryLogResponse>builder()
                .success(true)
                .message("Delivery log updated successfully")
                .data(data)
                .build());
    }

    @GetMapping("/{broadcastId}/delivery-logs")
    @PreAuthorize(CommunicationRoles.BROADCAST_MANAGE)
    public ResponseEntity<ApiResponse<List<DeliveryLogResponse>>> getDeliveryLogs(
            @PathVariable UUID broadcastId) {
        List<DeliveryLogResponse> data = broadcastService.getDeliveryLogs(broadcastId);
        return ResponseEntity.ok(ApiResponse.<List<DeliveryLogResponse>>builder()
                .success(true)
                .message("Delivery logs retrieved successfully")
                .data(data)
                .build());
    }

    // US-09018: Pin urgent announcements to top of mobile app feeds
    @PutMapping("/{broadcastId}/pin")
    @PreAuthorize(CommunicationRoles.LEADERSHIP_OR_ADMIN)
    public ResponseEntity<ApiResponse<BroadcastResponse>> pinBroadcast(
            @PathVariable UUID broadcastId,
            @Valid @RequestBody PinBroadcastRequest request) {
        BroadcastResponse data = broadcastService.pinBroadcast(broadcastId, request);
        return ResponseEntity.ok(ApiResponse.<BroadcastResponse>builder()
                .success(true)
                .message("Broadcast pinned status updated successfully")
                .data(data)
                .build());
    }

    @GetMapping("/pinned")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<BroadcastResponse>>> getPinnedBroadcasts(
            @RequestParam String tenantId) {
        List<BroadcastResponse> data = broadcastService.getPinnedBroadcasts(tenantId);
        return ResponseEntity.ok(ApiResponse.<List<BroadcastResponse>>builder()
                .success(true)
                .message("Pinned broadcasts retrieved successfully")
                .data(data)
                .build());
    }

    // US-09019: Archive past communication broadcasts for regulatory compliance
    @PutMapping("/{broadcastId}/archive")
    @PreAuthorize(CommunicationRoles.BROADCAST_MANAGE)
    public ResponseEntity<ApiResponse<BroadcastResponse>> archiveBroadcast(
            @PathVariable UUID broadcastId,
            @Valid @RequestBody ArchiveBroadcastRequest request) {
        BroadcastResponse data = broadcastService.archiveBroadcast(broadcastId, request);
        return ResponseEntity.ok(ApiResponse.<BroadcastResponse>builder()
                .success(true)
                .message("Broadcast archival status updated successfully")
                .data(data)
                .build());
    }

    @GetMapping("/archived")
    @PreAuthorize(CommunicationRoles.BROADCAST_MANAGE)
    public ResponseEntity<ApiResponse<List<BroadcastResponse>>> getArchivedBroadcasts(
            @RequestParam String tenantId) {
        List<BroadcastResponse> data = broadcastService.getArchivedBroadcasts(tenantId);
        return ResponseEntity.ok(ApiResponse.<List<BroadcastResponse>>builder()
                .success(true)
                .message("Archived broadcasts retrieved successfully")
                .data(data)
                .build());
    }
}
