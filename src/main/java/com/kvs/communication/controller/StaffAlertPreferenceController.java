package com.kvs.communication.controller;

import com.kvs.core.dto.ApiResponse;
import com.kvs.communication.config.CommunicationRoles;
import com.kvs.communication.dto.StaffAlertPreferenceDto;
import com.kvs.communication.entity.StaffAlertPreference;
import com.kvs.communication.repo.StaffAlertPreferenceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Settings → Notifications: the school alerts each staff member receives. */
@RestController
@RequestMapping("/api/v1/communication/staff-alerts/preferences")
@RequiredArgsConstructor
public class StaffAlertPreferenceController {

    private final StaffAlertPreferenceRepository repository;

    @GetMapping
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<StaffAlertPreferenceDto>> get(@RequestParam("tenantId") String tenantId,
                                                                    @RequestParam("userId") String userId) {
        StaffAlertPreference p = repository.findByTenantIdAndUserId(tenantId, userId)
                .orElseGet(() -> StaffAlertPreference.builder().tenantId(tenantId).userId(userId).build());
        return ResponseEntity.ok(ApiResponse.<StaffAlertPreferenceDto>builder().success(true)
                .message("Alert preferences retrieved successfully").data(map(p)).build());
    }

    @PutMapping
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    @Transactional
    public ResponseEntity<ApiResponse<StaffAlertPreferenceDto>> save(@Valid @RequestBody StaffAlertPreferenceDto request) {
        StaffAlertPreference p = repository.findByTenantIdAndUserId(request.getTenantId(), request.getUserId())
                .orElseGet(() -> StaffAlertPreference.builder().tenantId(request.getTenantId()).userId(request.getUserId()).build());
        if (request.getAttendanceAlerts() != null) p.setAttendanceAlerts(request.getAttendanceAlerts());
        if (request.getFeeReminders() != null) p.setFeeReminders(request.getFeeReminders());
        if (request.getLeaveRequests() != null) p.setLeaveRequests(request.getLeaveRequests());
        if (request.getDailyDigest() != null) p.setDailyDigest(request.getDailyDigest());
        return ResponseEntity.ok(ApiResponse.<StaffAlertPreferenceDto>builder().success(true)
                .message("Alert preferences saved").data(map(repository.save(p))).build());
    }

    private static StaffAlertPreferenceDto map(StaffAlertPreference p) {
        return StaffAlertPreferenceDto.builder()
                .tenantId(p.getTenantId())
                .userId(p.getUserId())
                .attendanceAlerts(p.getAttendanceAlerts())
                .feeReminders(p.getFeeReminders())
                .leaveRequests(p.getLeaveRequests())
                .dailyDigest(p.getDailyDigest())
                .build();
    }
}
