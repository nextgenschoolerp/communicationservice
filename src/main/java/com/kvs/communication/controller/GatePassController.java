package com.kvs.communication.controller;

import com.kvs.core.dto.ApiResponse;
import com.kvs.communication.config.CommunicationRoles;
import com.kvs.communication.dto.*;
import com.kvs.communication.service.GatePassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/communication/gate-passes")
@RequiredArgsConstructor
public class GatePassController {

    private final GatePassService gatePassService;

    // US-09005: Generate digital gate passes on parent apps for early student pickups
    @PostMapping
    @PreAuthorize(CommunicationRoles.GATE_PASS_REQUEST)
    public ResponseEntity<ApiResponse<GatePassResponse>> generateGatePass(
            @Valid @RequestBody GatePassRequest request) {
        GatePassResponse data = gatePassService.generateGatePass(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<GatePassResponse>builder()
                        .success(true)
                        .message("Digital student gate pass generated successfully with QR code")
                        .data(data)
                        .build());
    }

    // US-09006: Scan gate pass QR codes at security gates to authorize student departure
    @PostMapping("/verify")
    @PreAuthorize(CommunicationRoles.SECURITY_SCAN)
    public ResponseEntity<ApiResponse<GatePassResponse>> verifyGatePass(
            @Valid @RequestBody GatePassVerifyRequest request) {
        GatePassResponse data = gatePassService.verifyAndAuthorizeDeparture(request);
        return ResponseEntity.ok(ApiResponse.<GatePassResponse>builder()
                .success(true)
                .message("Gate pass QR verified successfully, student departure authorized")
                .data(data)
                .build());
    }

    @GetMapping
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<GatePassResponse>>> getStudentGatePasses(
            @RequestParam String tenantId,
            @RequestParam UUID studentId) {
        List<GatePassResponse> data = gatePassService.getStudentGatePasses(tenantId, studentId);
        return ResponseEntity.ok(ApiResponse.<List<GatePassResponse>>builder()
                .success(true)
                .message("Student gate passes retrieved successfully")
                .data(data)
                .build());
    }

    // US-09017: Audit security gate entry and exit logs for unauthorized visitor attempts
    @PostMapping("/audit-logs")
    @PreAuthorize(CommunicationRoles.SECURITY_SCAN)
    public ResponseEntity<ApiResponse<GateAuditLogResponse>> recordGateAuditLog(
            @Valid @RequestBody GateAuditLogRequest request) {
        GateAuditLogResponse data = gatePassService.recordGateAuditLog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<GateAuditLogResponse>builder()
                        .success(true)
                        .message("Security perimeter gate audit event logged successfully")
                        .data(data)
                        .build());
    }

    @GetMapping("/audit-logs")
    @PreAuthorize(CommunicationRoles.SECURITY_SCAN)
    public ResponseEntity<ApiResponse<List<GateAuditLogResponse>>> getGateAuditLogs(
            @RequestParam String tenantId) {
        List<GateAuditLogResponse> data = gatePassService.getGateAuditLogs(tenantId);
        return ResponseEntity.ok(ApiResponse.<List<GateAuditLogResponse>>builder()
                .success(true)
                .message("Perimeter gate audit logs retrieved successfully")
                .data(data)
                .build());
    }
}
