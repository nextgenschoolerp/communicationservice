package com.kvs.communication.controller;

import com.kvs.core.dto.ApiResponse;
import com.kvs.communication.config.CommunicationRoles;
import com.kvs.communication.dto.AbsenceAlertRequest;
import com.kvs.communication.dto.AbsenceAlertResponse;
import com.kvs.communication.service.AbsenceAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/communication/absence-alerts")
@RequiredArgsConstructor
public class AbsenceAlertController {

    private final AbsenceAlertService absenceAlertService;

    // US-09001: Dispatch automated alerts within 30 minutes of unexcused student absence
    @PostMapping
    @PreAuthorize(CommunicationRoles.SYSTEM_OR_ADMIN)
    public ResponseEntity<ApiResponse<AbsenceAlertResponse>> dispatchAbsenceAlert(
            @Valid @RequestBody AbsenceAlertRequest request) {
        AbsenceAlertResponse data = absenceAlertService.dispatchAbsenceAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AbsenceAlertResponse>builder()
                        .success(true)
                        .message("30-minute absence alert dispatched successfully")
                        .data(data)
                        .build());
    }

    @PostMapping("/{alertId}/escalate")
    @PreAuthorize(CommunicationRoles.SYSTEM_OR_ADMIN)
    public ResponseEntity<ApiResponse<AbsenceAlertResponse>> escalateAlert(
            @PathVariable UUID alertId) {
        AbsenceAlertResponse data = absenceAlertService.escalateAlert(alertId);
        return ResponseEntity.ok(ApiResponse.<AbsenceAlertResponse>builder()
                .success(true)
                .message("Absence alert escalated to school counselor")
                .data(data)
                .build());
    }

    @GetMapping
    @PreAuthorize(CommunicationRoles.SYSTEM_OR_ADMIN)
    public ResponseEntity<ApiResponse<List<AbsenceAlertResponse>>> getAbsenceAlerts(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AbsenceAlertResponse> data = absenceAlertService.getAbsenceAlerts(tenantId, date);
        return ResponseEntity.ok(ApiResponse.<List<AbsenceAlertResponse>>builder()
                .success(true)
                .message("Absence alerts retrieved successfully")
                .data(data)
                .build());
    }
}
