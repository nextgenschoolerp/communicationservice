package com.kvs.communication.service;

import com.kvs.core.exception.ResourceNotFoundException;
import com.kvs.communication.dto.*;
import com.kvs.communication.entity.*;
import com.kvs.communication.enums.TicketPriority;
import com.kvs.communication.enums.TicketStatus;
import com.kvs.communication.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommunityEngagementService {

    private final CalendarEventRepository calendarRepository;
    private final GrievanceTicketRepository grievanceRepository;
    private final DailyBulletinPostRepository bulletinRepository;
    private final FeedbackSurveyRepository surveyRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final DocumentNotificationRepository documentNotificationRepository;
    private final CommunicationAuditService auditService;

    // US-09008: Publish school calendar events, exam schedules, and photo galleries
    @Transactional
    public CalendarEventResponse createCalendarEvent(CalendarEventRequest request) {
        CalendarEvent event = CalendarEvent.builder()
                .tenantId(request.getTenantId())
                .title(request.getTitle())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .venue(request.getVenue())
                .targetAudience(request.getTargetAudience() != null ? request.getTargetAudience() : "ALL")
                .mediaUrls(request.getMediaUrls())
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                .build();

        event = calendarRepository.save(event);

        auditService.logSuccess("CalendarEvent", "CREATE_EVENT",
                "Published calendar event '" + event.getTitle() + "' (" + event.getStartDate() + " to " + event.getEndDate() + ")");

        return mapToEventResponse(event);
    }

    @Transactional(readOnly = true)
    public List<CalendarEventResponse> listPublishedEvents(UUID tenantId) {
        return calendarRepository.findByTenantIdAndIsPublishedTrueOrderByStartDateAsc(tenantId).stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    // US-09009: Log parent grievance support tickets with automated SLA escalation rules
    @Transactional
    public GrievanceTicketResponse createGrievanceTicket(GrievanceTicketRequest request) {
        GrievanceTicket ticket = GrievanceTicket.builder()
                .tenantId(request.getTenantId())
                .parentUserId(request.getParentUserId())
                .parentName(request.getParentName())
                .studentId(request.getStudentId())
                .studentName(request.getStudentName())
                .category(request.getCategory())
                .priority(request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM)
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .slaDueHours(request.getSlaDueHours() != null ? request.getSlaDueHours() : 48)
                .build();

        ticket = grievanceRepository.save(ticket);

        auditService.logSuccess("GrievanceTicket", "CREATE_TICKET",
                "Created parent grievance ticket ID " + ticket.getTicketId() + " (" + ticket.getCategory() + ")");

        return mapToGrievanceResponse(ticket);
    }

    @Transactional
    public GrievanceTicketResponse updateGrievanceTicket(UUID ticketId, GrievanceUpdateRequest request) {
        GrievanceTicket ticket = grievanceRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance ticket not found with ID: " + ticketId));

        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
            if (request.getStatus() == TicketStatus.RESOLVED) {
                ticket.setResolvedAt(Instant.now());
            }
        }
        if (request.getAssignedToUserId() != null) {
            ticket.setAssignedToUserId(request.getAssignedToUserId());
        }
        if (request.getResolutionNotes() != null) {
            ticket.setResolutionNotes(request.getResolutionNotes());
        }

        ticket = grievanceRepository.save(ticket);

        auditService.logSuccess("GrievanceTicket", "UPDATE_TICKET",
                "Updated ticket ID " + ticketId + " status=" + ticket.getStatus());

        return mapToGrievanceResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<GrievanceTicketResponse> listGrievances(UUID tenantId) {
        return grievanceRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::mapToGrievanceResponse)
                .collect(Collectors.toList());
    }

    // US-09013: Publish daily cafeteria menus and school bus schedule updates
    @Transactional
    public DailyBulletinResponse createDailyBulletin(DailyBulletinRequest request) {
        DailyBulletinPost post = DailyBulletinPost.builder()
                .tenantId(request.getTenantId())
                .bulletinType(request.getBulletinType())
                .title(request.getTitle())
                .contentDetails(request.getContentDetails())
                .effectiveDate(request.getEffectiveDate())
                .mealType(request.getMealType())
                .busRouteNumber(request.getBusRouteNumber())
                .build();

        post = bulletinRepository.save(post);

        auditService.logSuccess("DailyBulletinPost", "PUBLISH_BULLETIN",
                "Published bulletin ID " + post.getBulletinId() + " type=" + post.getBulletinType());

        return mapToBulletinResponse(post);
    }

    @Transactional(readOnly = true)
    public List<DailyBulletinResponse> listDailyBulletins(UUID tenantId, LocalDate date) {
        LocalDate searchDate = date != null ? date : LocalDate.now();
        return bulletinRepository.findByTenantIdAndEffectiveDate(tenantId, searchDate).stream()
                .map(this::mapToBulletinResponse)
                .collect(Collectors.toList());
    }

    // US-09014: Collect parent feedback surveys on school events and academic programs
    @Transactional
    public FeedbackSurveyResponse createSurvey(FeedbackSurveyRequest request) {
        FeedbackSurvey survey = FeedbackSurvey.builder()
                .tenantId(request.getTenantId())
                .title(request.getTitle())
                .description(request.getDescription())
                .surveyType(request.getSurveyType() != null ? request.getSurveyType() : "ACADEMIC")
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        survey = surveyRepository.save(survey);

        auditService.logSuccess("FeedbackSurvey", "CREATE_SURVEY",
                "Created survey '" + survey.getTitle() + "'");

        return mapToSurveyResponse(survey);
    }

    @Transactional
    public SurveyResponseDto submitSurveyResponse(SurveySubmissionRequest request) {
        FeedbackSurvey survey = surveyRepository.findById(request.getSurveyId())
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + request.getSurveyId()));

        SurveyResponse response = SurveyResponse.builder()
                .tenantId(request.getTenantId())
                .surveyId(survey.getSurveyId())
                .parentUserId(request.getParentUserId())
                .parentName(request.getParentName())
                .ratingScore(request.getRatingScore())
                .comments(request.getComments())
                .build();

        response = surveyResponseRepository.save(response);

        auditService.logSuccess("SurveyResponse", "SUBMIT_FEEDBACK",
                "Parent " + response.getParentName() + " submitted feedback for survey ID " + survey.getSurveyId());

        return SurveyResponseDto.builder()
                .responseId(response.getResponseId())
                .tenantId(response.getTenantId())
                .surveyId(response.getSurveyId())
                .parentUserId(response.getParentUserId())
                .parentName(response.getParentName())
                .ratingScore(response.getRatingScore())
                .comments(response.getComments())
                .submittedAt(response.getCreatedAt() != null ? response.getCreatedAt() : Instant.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<SurveyResponseDto> listSurveyResponses(UUID surveyId) {
        return surveyResponseRepository.findBySurveyId(surveyId).stream()
                .map(r -> SurveyResponseDto.builder()
                        .responseId(r.getResponseId())
                        .tenantId(r.getTenantId())
                        .surveyId(r.getSurveyId())
                        .parentUserId(r.getParentUserId())
                        .parentName(r.getParentName())
                        .ratingScore(r.getRatingScore())
                        .comments(r.getComments())
                        .submittedAt(r.getCreatedAt() != null ? r.getCreatedAt() : Instant.now())
                        .build())
                .collect(Collectors.toList());
    }

    // US-09015: Notify parents instantly when digital fee receipts or report cards are issued
    @Transactional
    public DocumentNotificationResponse sendDocumentNotification(DocumentNotificationRequest request) {
        DocumentNotification notif = DocumentNotification.builder()
                .tenantId(request.getTenantId())
                .documentType(request.getDocumentType())
                .documentId(request.getDocumentId())
                .studentId(request.getStudentId())
                .parentPhone(request.getParentPhone())
                .parentEmail(request.getParentEmail())
                .downloadUrl(request.getDownloadUrl())
                .deliveryStatus("DELIVERED")
                .build();

        notif = documentNotificationRepository.save(notif);

        auditService.logSuccess("DocumentNotification", "SEND_DOCUMENT_ALERT",
                "Dispatched document notification for " + notif.getDocumentType() + " (" + notif.getDocumentId() + ") to " + notif.getParentPhone());

        return DocumentNotificationResponse.builder()
                .notificationId(notif.getNotificationId())
                .tenantId(notif.getTenantId())
                .documentType(notif.getDocumentType())
                .documentId(notif.getDocumentId())
                .studentId(notif.getStudentId())
                .parentPhone(notif.getParentPhone())
                .parentEmail(notif.getParentEmail())
                .downloadUrl(notif.getDownloadUrl())
                .deliveryStatus(notif.getDeliveryStatus())
                .sentAt(notif.getCreatedAt() != null ? notif.getCreatedAt() : Instant.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<DocumentNotificationResponse> listDocumentNotifications(UUID tenantId, UUID studentId) {
        return documentNotificationRepository.findByTenantIdAndStudentId(tenantId, studentId).stream()
                .map(d -> DocumentNotificationResponse.builder()
                        .notificationId(d.getNotificationId())
                        .tenantId(d.getTenantId())
                        .documentType(d.getDocumentType())
                        .documentId(d.getDocumentId())
                        .studentId(d.getStudentId())
                        .parentPhone(d.getParentPhone())
                        .parentEmail(d.getParentEmail())
                        .downloadUrl(d.getDownloadUrl())
                        .deliveryStatus(d.getDeliveryStatus())
                        .sentAt(d.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private CalendarEventResponse mapToEventResponse(CalendarEvent e) {
        return CalendarEventResponse.builder()
                .eventId(e.getEventId())
                .tenantId(e.getTenantId())
                .title(e.getTitle())
                .description(e.getDescription())
                .eventType(e.getEventType())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .venue(e.getVenue())
                .targetAudience(e.getTargetAudience())
                .mediaUrls(e.getMediaUrls())
                .isPublished(e.getIsPublished())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private GrievanceTicketResponse mapToGrievanceResponse(GrievanceTicket t) {
        return GrievanceTicketResponse.builder()
                .ticketId(t.getTicketId())
                .tenantId(t.getTenantId())
                .parentUserId(t.getParentUserId())
                .parentName(t.getParentName())
                .studentId(t.getStudentId())
                .studentName(t.getStudentName())
                .category(t.getCategory())
                .priority(t.getPriority())
                .subject(t.getSubject())
                .description(t.getDescription())
                .status(t.getStatus())
                .slaDueHours(t.getSlaDueHours())
                .assignedToUserId(t.getAssignedToUserId())
                .resolutionNotes(t.getResolutionNotes())
                .createdAt(t.getCreatedAt())
                .resolvedAt(t.getResolvedAt())
                .build();
    }

    private DailyBulletinResponse mapToBulletinResponse(DailyBulletinPost b) {
        return DailyBulletinResponse.builder()
                .bulletinId(b.getBulletinId())
                .tenantId(b.getTenantId())
                .bulletinType(b.getBulletinType())
                .title(b.getTitle())
                .contentDetails(b.getContentDetails())
                .effectiveDate(b.getEffectiveDate())
                .mealType(b.getMealType())
                .busRouteNumber(b.getBusRouteNumber())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private FeedbackSurveyResponse mapToSurveyResponse(FeedbackSurvey s) {
        return FeedbackSurveyResponse.builder()
                .surveyId(s.getSurveyId())
                .tenantId(s.getTenantId())
                .title(s.getTitle())
                .description(s.getDescription())
                .surveyType(s.getSurveyType())
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
