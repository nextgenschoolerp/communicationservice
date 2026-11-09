package com.kvs.communication.controller;

import com.kvs.communication.config.CommunicationPerms;
import com.kvs.core.dto.ApiResponse;
import com.kvs.communication.dto.*;
import com.kvs.communication.service.MobileAppService;
import com.kvs.communication.service.ParentAccessGuard;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/communication/mobile")
@RequiredArgsConstructor
public class MobileAppController {

    private final MobileAppService mobileAppService;
    private final ParentAccessGuard parentAccessGuard;

    // US-09002: Register mobile app push notification device tokens
    @PostMapping("/devices")
    @PreAuthorize(CommunicationPerms.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<DeviceRegistrationResponse>> registerDevice(
            @Valid @RequestBody DeviceRegistrationRequest request) {
        DeviceRegistrationResponse data = mobileAppService.registerDeviceToken(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DeviceRegistrationResponse>builder()
                        .success(true)
                        .message("Device token registered successfully for push notifications")
                        .data(data)
                        .build());
    }

    // US-09003: Multi-child profile switching under single parent credentials
    @PostMapping("/child-mappings")
    @PreAuthorize(CommunicationPerms.MOBILE_MANAGE)
    public ResponseEntity<ApiResponse<ChildProfileResponse>> mapParentChild(
            @Valid @RequestBody ParentChildMappingRequest request) {
        ChildProfileResponse data = mobileAppService.mapParentStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ChildProfileResponse>builder()
                        .success(true)
                        .message("Child profile mapped successfully to parent account")
                        .data(data)
                        .build());
    }

    @GetMapping({"/parent/{parentId}/wards", "/parents/{parentId}/children", "/parents/{parentId}/wards"})
    @PreAuthorize(CommunicationPerms.MOBILE_VIEW)
    public ResponseEntity<ApiResponse<List<ChildProfileResponse>>> getLinkedChildren(
            @PathVariable String parentId) {
        parentAccessGuard.requireSelfOrStaff(parentId);
        List<ChildProfileResponse> data = mobileAppService.getLinkedChildren(parentId);
        return ResponseEntity.ok(ApiResponse.<List<ChildProfileResponse>>builder()
                .success(true)
                .message("Linked child profiles retrieved successfully for account switching")
                .data(data)
                .build());
    }

    // US-09012: Configure notification preferences for parents
    @PostMapping("/preferences")
    @PreAuthorize(CommunicationPerms.MOBILE_MANAGE)
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> configurePreferences(
            @Valid @RequestBody NotificationPreferenceRequest request) {
        NotificationPreferenceResponse data = mobileAppService.configurePreferences(request);
        return ResponseEntity.ok(ApiResponse.<NotificationPreferenceResponse>builder()
                .success(true)
                .message("Notification preferences updated successfully")
                .data(data)
                .build());
    }

    @GetMapping("/preferences")
    @PreAuthorize(CommunicationPerms.MOBILE_VIEW)
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(
            @RequestParam String tenantId,
            @RequestParam String userId) {
        NotificationPreferenceResponse data = mobileAppService.getPreferences(tenantId, userId);
        return ResponseEntity.ok(ApiResponse.<NotificationPreferenceResponse>builder()
                .success(true)
                .message("Notification preferences retrieved successfully")
                .data(data)
                .build());
    }
}
