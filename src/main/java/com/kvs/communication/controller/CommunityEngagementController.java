package com.kvs.communication.controller;

import com.kvs.core.dto.ApiResponse;
import com.kvs.communication.config.CommunicationRoles;
import com.kvs.communication.dto.*;
import com.kvs.communication.service.CommunityEngagementService;
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
@RequestMapping("/api/v1/communication/community")
@RequiredArgsConstructor
public class CommunityEngagementController {

    private final CommunityEngagementService communityService;

    // US-09008: Publish school calendar events, exam schedules, and photo galleries
    @PostMapping("/calendar-events")
    @PreAuthorize(CommunicationRoles.BROADCAST_MANAGE)
    public ResponseEntity<ApiResponse<CalendarEventResponse>> createCalendarEvent(
            @Valid @RequestBody CalendarEventRequest request) {
        CalendarEventResponse data = communityService.createCalendarEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CalendarEventResponse>builder()
                        .success(true)
                        .message("School calendar event published successfully")
                        .data(data)
                        .build());
    }

    @PutMapping("/calendar-events/{eventId}")
    @PreAuthorize(CommunicationRoles.BROADCAST_MANAGE)
    public ResponseEntity<ApiResponse<CalendarEventResponse>> updateCalendarEvent(
            @PathVariable("eventId") UUID eventId, @Valid @RequestBody CalendarEventRequest request) {
        return ResponseEntity.ok(ApiResponse.<CalendarEventResponse>builder()
                .success(true)
                .message("School calendar event updated successfully")
                .data(communityService.updateCalendarEvent(eventId, request))
                .build());
    }

    @GetMapping("/calendar-events")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<CalendarEventResponse>>> listCalendarEvents(
            @RequestParam UUID tenantId) {
        List<CalendarEventResponse> data = communityService.listPublishedEvents(tenantId);
        return ResponseEntity.ok(ApiResponse.<List<CalendarEventResponse>>builder()
                .success(true)
                .message("School calendar events retrieved successfully")
                .data(data)
                .build());
    }

    // US-09009: Log parent grievance support tickets with automated SLA escalation rules
    @PostMapping("/grievances")
    @PreAuthorize(CommunicationRoles.PARENT_ONLY)
    public ResponseEntity<ApiResponse<GrievanceTicketResponse>> createGrievance(
            @Valid @RequestBody GrievanceTicketRequest request) {
        GrievanceTicketResponse data = communityService.createGrievanceTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<GrievanceTicketResponse>builder()
                        .success(true)
                        .message("Parent grievance ticket registered with automated SLA tracking")
                        .data(data)
                        .build());
    }

    @PutMapping("/grievances/{ticketId}")
    @PreAuthorize(CommunicationRoles.RECEPTION_DESK)
    public ResponseEntity<ApiResponse<GrievanceTicketResponse>> updateGrievance(
            @PathVariable UUID ticketId,
            @Valid @RequestBody GrievanceUpdateRequest request) {
        GrievanceTicketResponse data = communityService.updateGrievanceTicket(ticketId, request);
        return ResponseEntity.ok(ApiResponse.<GrievanceTicketResponse>builder()
                .success(true)
                .message("Grievance ticket status and assignment updated successfully")
                .data(data)
                .build());
    }

    @GetMapping("/grievances")
    @PreAuthorize(CommunicationRoles.RECEPTION_DESK)
    public ResponseEntity<ApiResponse<List<GrievanceTicketResponse>>> listGrievances(
            @RequestParam UUID tenantId) {
        List<GrievanceTicketResponse> data = communityService.listGrievances(tenantId);
        return ResponseEntity.ok(ApiResponse.<List<GrievanceTicketResponse>>builder()
                .success(true)
                .message("Parent grievance tickets retrieved successfully")
                .data(data)
                .build());
    }

    // US-09013: Publish daily cafeteria menus and school bus schedule updates
    @PostMapping("/bulletins")
    @PreAuthorize(CommunicationRoles.RECEPTION_DESK)
    public ResponseEntity<ApiResponse<DailyBulletinResponse>> createBulletin(
            @Valid @RequestBody DailyBulletinRequest request) {
        DailyBulletinResponse data = communityService.createDailyBulletin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DailyBulletinResponse>builder()
                        .success(true)
                        .message("Daily campus bulletin published successfully")
                        .data(data)
                        .build());
    }

    @GetMapping("/bulletins")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<DailyBulletinResponse>>> listBulletins(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DailyBulletinResponse> data = communityService.listDailyBulletins(tenantId, date);
        return ResponseEntity.ok(ApiResponse.<List<DailyBulletinResponse>>builder()
                .success(true)
                .message("Daily bulletins retrieved successfully")
                .data(data)
                .build());
    }

    // US-09014: Collect parent feedback surveys on school events and academic programs
    @PostMapping("/surveys")
    @PreAuthorize(CommunicationRoles.BROADCAST_MANAGE)
    public ResponseEntity<ApiResponse<FeedbackSurveyResponse>> createSurvey(
            @Valid @RequestBody FeedbackSurveyRequest request) {
        FeedbackSurveyResponse data = communityService.createSurvey(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<FeedbackSurveyResponse>builder()
                        .success(true)
                        .message("Feedback survey template created successfully")
                        .data(data)
                        .build());
    }

    @PostMapping("/surveys/responses")
    @PreAuthorize(CommunicationRoles.PARENT_ONLY)
    public ResponseEntity<ApiResponse<SurveyResponseDto>> submitSurveyResponse(
            @Valid @RequestBody SurveySubmissionRequest request) {
        SurveyResponseDto data = communityService.submitSurveyResponse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SurveyResponseDto>builder()
                        .success(true)
                        .message("Survey feedback submitted successfully")
                        .data(data)
                        .build());
    }

    @GetMapping("/surveys/{surveyId}/responses")
    @PreAuthorize(CommunicationRoles.BROADCAST_MANAGE)
    public ResponseEntity<ApiResponse<List<SurveyResponseDto>>> listSurveyResponses(
            @PathVariable UUID surveyId) {
        List<SurveyResponseDto> data = communityService.listSurveyResponses(surveyId);
        return ResponseEntity.ok(ApiResponse.<List<SurveyResponseDto>>builder()
                .success(true)
                .message("Survey responses retrieved successfully")
                .data(data)
                .build());
    }

    // US-09015: Notify parents instantly when digital fee receipts or report cards are issued
    @PostMapping("/documents/notifications")
    @PreAuthorize(CommunicationRoles.DOCUMENT_DISPATCH)
    public ResponseEntity<ApiResponse<DocumentNotificationResponse>> sendDocumentNotification(
            @Valid @RequestBody DocumentNotificationRequest request) {
        DocumentNotificationResponse data = communityService.sendDocumentNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DocumentNotificationResponse>builder()
                        .success(true)
                        .message("Document dispatch notification sent to guardian successfully")
                        .data(data)
                        .build());
    }

    @GetMapping("/documents/notifications")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<DocumentNotificationResponse>>> listDocumentNotifications(
            @RequestParam UUID tenantId,
            @RequestParam UUID studentId) {
        List<DocumentNotificationResponse> data = communityService.listDocumentNotifications(tenantId, studentId);
        return ResponseEntity.ok(ApiResponse.<List<DocumentNotificationResponse>>builder()
                .success(true)
                .message("Document notifications retrieved successfully")
                .data(data)
                .build());
    }
}
