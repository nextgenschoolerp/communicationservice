package com.kvs.communication.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.kvs.communication.dto.CalendarEventRequest;
import com.kvs.communication.dto.SupportMessageRequest;
import com.kvs.communication.dto.SupportStatusRequest;
import com.kvs.communication.dto.SupportTicketRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SupportAndCalendarIntegrationTest extends CommunicationTestSupport {

    @Test
    @DisplayName("Help: raise a support ticket, read its conversation, reply and resolve it")
    void testSupportTickets() throws Exception {
        String userId = "u-help-" + run;
        String staff = token("ADMIN_CLERK", userId);

        SupportTicketRequest req = SupportTicketRequest.builder()
                .tenantId(tenantId).requesterUserId(userId).requesterName("Meena Kumari")
                .subject("Cannot export the fee statement").category("FEES").description("The CSV opens with garbled pound signs.").build();
        JsonNode created = data(postJson("/support/tickets", staff, req).andExpect(status().isCreated()));
        String ticketId = created.get("ticketId").asText();
        assertThat(created.get("ticketNumber").asText()).startsWith("SUP-");
        assertThat(created.get("status").asText()).isEqualTo("OPEN");
        assertThat(created.get("messages").size()).isEqualTo(1);

        req.setCategory("NOPE");
        postJson("/support/tickets", staff, req).andExpect(status().isBadRequest());

        JsonNode reply = data(postJson("/support/tickets/" + ticketId + "/messages", staff,
                new SupportMessageRequest("Support team", "SUPPORT", "Thanks, we are looking into it.")).andExpect(status().isCreated()));
        assertThat(reply.get("status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(reply.get("messages").size()).isEqualTo(2);

        JsonNode resolved = data(putJson("/support/tickets/" + ticketId + "/status", staff, new SupportStatusRequest("RESOLVED")).andExpect(status().isOk()));
        assertThat(resolved.get("resolvedAt").isNull()).isFalse();

        // Replying to a resolved ticket reopens it
        JsonNode reopened = data(postJson("/support/tickets/" + ticketId + "/messages", staff,
                new SupportMessageRequest("Meena Kumari", null, "It happened again today.")).andExpect(status().isCreated()));
        assertThat(reopened.get("status").asText()).isEqualTo("OPEN");

        JsonNode mine = data(getAs("/support/tickets", staff, "tenantId", tenantId.toString(), "requesterUserId", userId).andExpect(status().isOk()));
        assertThat(mine.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Settings → Notifications: a staff member's alert switches default on and are saved per user")
    void testStaffAlertPreferences() throws Exception {
        String userId = "u1111111-1111-1111-1111-111111111111";
        String admin = token("SUPER_ADMIN", userId);
        JsonNode defaults = data(getAs("/staff-alerts/preferences", admin, "tenantId", tenantId.toString(), "userId", userId).andExpect(status().isOk()));
        assertThat(defaults.get("attendanceAlerts").asBoolean()).isTrue();
        assertThat(defaults.get("dailyDigest").asBoolean()).isFalse();

        java.util.Map<String, Object> change = java.util.Map.of("tenantId", tenantId, "userId", userId, "dailyDigest", true, "feeReminders", false);
        data(putJson("/staff-alerts/preferences", admin, change).andExpect(status().isOk()));
        JsonNode saved = data(getAs("/staff-alerts/preferences", admin, "tenantId", tenantId.toString(), "userId", userId).andExpect(status().isOk()));
        assertThat(saved.get("dailyDigest").asBoolean()).isTrue();
        assertThat(saved.get("feeReminders").asBoolean()).isFalse();
        assertThat(saved.get("attendanceAlerts").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("Calendar: edit an event's dates, times and organiser")
    void testUpdateCalendarEvent() throws Exception {
        String pro = token("PUBLIC_RELATIONS_OFFICER");
        CalendarEventRequest req = CalendarEventRequest.builder()
                .tenantId(tenantId).title("Year 10 parent–teacher meetings").eventType("MEETING")
                .startDate(LocalDate.of(2026, 10, 19)).endDate(LocalDate.of(2026, 10, 19))
                .startTime("15:30").endTime("18:00").venue("Main hall").organiser("Michael Brooks").targetAudience("GRADE_10").build();
        JsonNode event = data(postJson("/community/calendar-events", pro, req).andExpect(status().isCreated()));
        assertThat(event.get("startTime").asText()).isEqualTo("15:30");

        req.setTitle("Year 10 parent–teacher evening");
        req.setEndTime("18:30");
        JsonNode updated = data(putJson("/community/calendar-events/" + event.get("eventId").asText(), pro, req).andExpect(status().isOk()));
        assertThat(updated.get("title").asText()).isEqualTo("Year 10 parent–teacher evening");
        assertThat(updated.get("endTime").asText()).isEqualTo("18:30");
        assertThat(updated.get("organiser").asText()).isEqualTo("Michael Brooks");

        req.setEndTime("14:00");
        putJson("/community/calendar-events/" + event.get("eventId").asText(), pro, req).andExpect(status().isBadRequest());
        req.setEndTime("25:00");
        putJson("/community/calendar-events/" + event.get("eventId").asText(), pro, req).andExpect(status().isBadRequest());
        req.setEndTime("18:30");
        putJson("/community/calendar-events/" + event.get("eventId").asText(), token("PARENT"), req).andExpect(status().isForbidden());
    }
}
