package com.kvs.communication.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.kvs.communication.dto.*;
import com.kvs.communication.enums.TicketPriority;
import com.kvs.communication.enums.TicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommunityEngagementIntegrationTest extends CommunicationTestSupport {

    @Test
    @DisplayName("US-09004: Facilitate secure two-way confidential messaging between parents and class teachers")
    void testTeacherParentMessaging() throws Exception {
        String parentToken = token("PARENT");
        String teacherToken = token("TEACHER");

        UUID threadId = UUID.randomUUID();

        // Parent sends message to teacher
        SendMessageRequest parentMsg = SendMessageRequest.builder()
                .tenantId(tenantId)
                .senderId(parentUserId)
                .senderName("Rajesh Sharma")
                .senderRole("ROLE_PARENT")
                .recipientId(teacherUserId)
                .recipientName("Dr. Kavya Nair")
                .studentId(studentId)
                .threadId(threadId)
                .subject("Query regarding Mathematics Quiz 2 Syllabus")
                .content("Dear Ma'am, please let us know if chapter 4 polynomials will be included in Monday's quiz.")
                .build();

        JsonNode msgNode = data(postJson("/messages", parentToken, parentMsg).andExpect(status().isCreated()));
        UUID messageId = UUID.fromString(msgNode.get("messageId").asText());
        assertThat(msgNode.get("isRead").asBoolean()).isFalse();

        // Teacher replies
        SendMessageRequest teacherReply = SendMessageRequest.builder()
                .tenantId(tenantId)
                .senderId(teacherUserId)
                .senderName("Dr. Kavya Nair")
                .senderRole("ROLE_TEACHER")
                .recipientId(parentUserId)
                .recipientName("Rajesh Sharma")
                .studentId(studentId)
                .threadId(threadId)
                .subject("RE: Query regarding Mathematics Quiz 2 Syllabus")
                .content("Dear Mr. Sharma, only chapters 1 to 3 are included in the upcoming quiz.")
                .build();
        data(postJson("/messages", teacherToken, teacherReply).andExpect(status().isCreated()));

        // Mark first message read
        JsonNode readMsg = data(putJson("/messages/" + messageId + "/read", teacherToken, null).andExpect(status().isOk()));
        assertThat(readMsg.get("isRead").asBoolean()).isTrue();

        // Get conversation thread
        JsonNode thread = data(getAs("/messages/threads/" + threadId, parentToken).andExpect(status().isOk()));
        assertThat(thread.size()).isEqualTo(2);

        // Get inbox
        JsonNode inbox = data(getAs("/messages/inbox/" + parentUserId, parentToken).andExpect(status().isOk()));
        assertThat(inbox.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("US-09008: Publish school calendar events, exam schedules, and photo galleries")
    void testCalendarEvents() throws Exception {
        String proToken = token("PUBLIC_RELATIONS_OFFICER");

        CalendarEventRequest req = CalendarEventRequest.builder()
                .tenantId(tenantId)
                .title("Term 1 Final Board Examinations 2026")
                .description("Comprehensive syllabus exams for grades 9 through 12.")
                .eventType("EXAM")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .venue("Senior Academic Block")
                .targetAudience("GRADES_9_TO_12")
                .mediaUrls("https://cdn.schoolerp.internal/schedules/term1-exam-date-sheet.pdf")
                .isPublished(true)
                .build();

        JsonNode eventNode = data(postJson("/community/calendar-events", proToken, req).andExpect(status().isCreated()));
        assertThat(eventNode.get("title").asText()).isEqualTo("Term 1 Final Board Examinations 2026");

        JsonNode events = data(getAs("/community/calendar-events", proToken, "tenantId", tenantId.toString()).andExpect(status().isOk()));
        assertThat(events.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("US-09009: Log parent grievance support tickets with automated SLA escalation rules")
    void testParentGrievances() throws Exception {
        String parentToken = token("PARENT");
        String deskToken = token("RECEPTIONIST");

        GrievanceTicketRequest req = GrievanceTicketRequest.builder()
                .tenantId(tenantId)
                .parentUserId(parentUserId)
                .parentName("Rajesh Sharma")
                .studentId(studentId)
                .studentName("Aarav Sharma")
                .category("TRANSPORT")
                .priority(TicketPriority.HIGH)
                .subject("Bus 14 consistently delayed at morning pickup point")
                .description("Bus 14 is arriving 25 minutes past scheduled time for the past three days.")
                .slaDueHours(24)
                .build();

        JsonNode ticketNode = data(postJson("/community/grievances", parentToken, req).andExpect(status().isCreated()));
        UUID ticketId = UUID.fromString(ticketNode.get("ticketId").asText());
        assertThat(ticketNode.get("status").asText()).isEqualTo("OPEN");
        assertThat(ticketNode.get("slaDueHours").asInt()).isEqualTo(24);

        // Update ticket to resolved
        GrievanceUpdateRequest updateReq = GrievanceUpdateRequest.builder()
                .status(TicketStatus.RESOLVED)
                .assignedToUserId(UUID.randomUUID())
                .resolutionNotes("Transport coordinator instructed driver to start 15 minutes earlier.")
                .build();

        JsonNode resolved = data(putJson("/community/grievances/" + ticketId, deskToken, updateReq).andExpect(status().isOk()));
        assertThat(resolved.get("status").asText()).isEqualTo("RESOLVED");

        // List grievances
        JsonNode list = data(getAs("/community/grievances", deskToken, "tenantId", tenantId.toString()).andExpect(status().isOk()));
        assertThat(list.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("US-09013: Publish daily cafeteria menus and school bus schedule updates")
    void testDailyBulletins() throws Exception {
        String deskToken = token("RECEPTIONIST");
        String parentToken = token("PARENT");

        DailyBulletinRequest req = DailyBulletinRequest.builder()
                .tenantId(tenantId)
                .bulletinType("CAFETERIA_MENU")
                .title("Nutritious Wednesday Lunch Menu")
                .contentDetails("Paneer Butter Masala, Whole Wheat Roti, Jeera Rice, Dal Tadka, Fresh Fruit Salad.")
                .effectiveDate(LocalDate.now())
                .mealType("LUNCH")
                .build();

        JsonNode bNode = data(postJson("/community/bulletins", deskToken, req).andExpect(status().isCreated()));
        assertThat(bNode.get("mealType").asText()).isEqualTo("LUNCH");

        JsonNode list = data(getAs("/community/bulletins", parentToken, "tenantId", tenantId.toString(), "date", LocalDate.now().toString()).andExpect(status().isOk()));
        assertThat(list.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("US-09014: Collect parent feedback surveys on school events and academic programs")
    void testParentFeedbackSurveys() throws Exception {
        String proToken = token("PUBLIC_RELATIONS_OFFICER");
        String parentToken = token("PARENT");

        FeedbackSurveyRequest surveyReq = FeedbackSurveyRequest.builder()
                .tenantId(tenantId)
                .title("Annual Science Exhibition 2026 Feedback")
                .description("Please share your feedback on the student scientific models and organisation.")
                .surveyType("EVENT")
                .isActive(true)
                .build();

        JsonNode sNode = data(postJson("/community/surveys", proToken, surveyReq).andExpect(status().isCreated()));
        UUID surveyId = UUID.fromString(sNode.get("surveyId").asText());

        // Parent submits survey response
        SurveySubmissionRequest responseReq = SurveySubmissionRequest.builder()
                .tenantId(tenantId)
                .surveyId(surveyId)
                .parentUserId(parentUserId)
                .parentName("Rajesh Sharma")
                .ratingScore(5)
                .comments("Excellently organised! The robotics demo by Grade 10 was inspiring.")
                .build();

        JsonNode respNode = data(postJson("/community/surveys/responses", parentToken, responseReq).andExpect(status().isCreated()));
        assertThat(respNode.get("ratingScore").asInt()).isEqualTo(5);

        // Query responses
        JsonNode responses = data(getAs("/community/surveys/" + surveyId + "/responses", proToken).andExpect(status().isOk()));
        assertThat(responses.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("US-09015: Notify parents instantly when digital fee receipts or report cards are issued")
    void testDocumentDispatchNotification() throws Exception {
        String sysToken = token("SYSTEM_AUTOMATION");
        String parentToken = token("PARENT");

        DocumentNotificationRequest req = DocumentNotificationRequest.builder()
                .tenantId(tenantId)
                .documentType("FEE_RECEIPT")
                .documentId("REC-2026-Q1-98765")
                .studentId(studentId)
                .parentPhone("+919876543210")
                .parentEmail("rajesh.sharma@kvs.local")
                .downloadUrl("https://portal.schoolerp.internal/documents/receipts/REC-2026-Q1-98765.pdf")
                .build();

        JsonNode docNode = data(postJson("/community/documents/notifications", sysToken, req).andExpect(status().isCreated()));
        assertThat(docNode.get("deliveryStatus").asText()).isEqualTo("DELIVERED");
        assertThat(docNode.get("documentType").asText()).isEqualTo("FEE_RECEIPT");

        // Query document notifications
        JsonNode list = data(getAs("/community/documents/notifications", parentToken, "tenantId", tenantId.toString(), "studentId", studentId.toString()).andExpect(status().isOk()));
        assertThat(list.size()).isGreaterThanOrEqualTo(1);
    }
}
