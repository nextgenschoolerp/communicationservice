package com.kvs.communication.controller;

import com.kvs.core.dto.ApiResponse;
import com.kvs.communication.config.CommunicationRoles;
import com.kvs.communication.dto.*;
import com.kvs.communication.service.VisitorService;
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
@RequestMapping("/api/v1/communication/visitors")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    // US-09007: Register visitor appointments and issue digital visitor passes at reception
    @PostMapping("/appointments")
    @PreAuthorize(CommunicationRoles.RECEPTION_DESK)
    public ResponseEntity<ApiResponse<VisitorAppointmentResponse>> registerVisitor(
            @Valid @RequestBody VisitorAppointmentRequest request) {
        VisitorAppointmentResponse data = visitorService.registerVisitor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<VisitorAppointmentResponse>builder()
                        .success(true)
                        .message("Visitor appointment registered and digital badge issued")
                        .data(data)
                        .build());
    }

    @PutMapping("/appointments/{appointmentId}/check-in")
    @PreAuthorize(CommunicationRoles.RECEPTION_DESK)
    public ResponseEntity<ApiResponse<VisitorAppointmentResponse>> checkInVisitor(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody VisitorCheckInRequest request) {
        VisitorAppointmentResponse data = visitorService.checkInVisitor(appointmentId, request);
        return ResponseEntity.ok(ApiResponse.<VisitorAppointmentResponse>builder()
                .success(true)
                .message("Visitor check-in confirmed successfully")
                .data(data)
                .build());
    }

    @PutMapping("/appointments/{appointmentId}/check-out")
    @PreAuthorize(CommunicationRoles.RECEPTION_DESK)
    public ResponseEntity<ApiResponse<VisitorAppointmentResponse>> checkOutVisitor(
            @PathVariable UUID appointmentId) {
        VisitorAppointmentResponse data = visitorService.checkOutVisitor(appointmentId);
        return ResponseEntity.ok(ApiResponse.<VisitorAppointmentResponse>builder()
                .success(true)
                .message("Visitor check-out confirmed successfully")
                .data(data)
                .build());
    }

    @GetMapping("/appointments")
    @PreAuthorize(CommunicationRoles.RECEPTION_DESK)
    public ResponseEntity<ApiResponse<List<VisitorAppointmentResponse>>> listAppointments(
            @RequestParam UUID tenantId) {
        List<VisitorAppointmentResponse> data = visitorService.listAppointments(tenantId);
        return ResponseEntity.ok(ApiResponse.<List<VisitorAppointmentResponse>>builder()
                .success(true)
                .message("Visitor appointments retrieved successfully")
                .data(data)
                .build());
    }

    // US-09016: Manage appointment booking slots for principal/leadership meetings
    @PostMapping("/slots")
    @PreAuthorize(CommunicationRoles.RECEPTION_DESK)
    public ResponseEntity<ApiResponse<AppointmentSlotResponse>> createSlot(
            @Valid @RequestBody AppointmentSlotRequest request) {
        AppointmentSlotResponse data = visitorService.createSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AppointmentSlotResponse>builder()
                        .success(true)
                        .message("Appointment meeting slot created successfully")
                        .data(data)
                        .build());
    }

    @PostMapping("/slots/{slotId}/book")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<AppointmentSlotResponse>> bookSlot(
            @PathVariable UUID slotId,
            @Valid @RequestBody BookSlotRequest request) {
        AppointmentSlotResponse data = visitorService.bookSlot(slotId, request);
        return ResponseEntity.ok(ApiResponse.<AppointmentSlotResponse>builder()
                .success(true)
                .message("Appointment slot booked successfully")
                .data(data)
                .build());
    }

    @GetMapping("/slots")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<AppointmentSlotResponse>>> getAvailableSlots(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentSlotResponse> data = visitorService.getAvailableSlots(tenantId, date);
        return ResponseEntity.ok(ApiResponse.<List<AppointmentSlotResponse>>builder()
                .success(true)
                .message("Available meeting slots retrieved successfully")
                .data(data)
                .build());
    }
}
