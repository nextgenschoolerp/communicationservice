package com.kvs.communication.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.kvs.communication.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GatePassAndVisitorIntegrationTest extends CommunicationTestSupport {

    @Test
    @DisplayName("US-09005 & US-09006: Generate digital gate pass on parent app and verify with security QR scan")
    void testGatePassLifecycle() throws Exception {
        String parentToken = token("PARENT");
        String securityToken = token("SECURITY_GUARD");

        GatePassRequest req = GatePassRequest.builder()
                .tenantId(tenantId)
                .studentId(studentId)
                .studentName("Aarav Sharma")
                .grade("10")
                .section("A")
                .guardianId(parentUserId)
                .guardianName("Rajesh Sharma")
                .pickupPersonName("Rajesh Sharma")
                .pickupPersonPhone("+919876543210")
                .reason("Medical doctor appointment scheduled at 2 PM")
                .build();

        JsonNode passNode = data(postJson("/gate-passes", parentToken, req).andExpect(status().isCreated()));
        UUID passId = UUID.fromString(passNode.get("passId").asText());
        String qrCode = passNode.get("qrCode").asText();
        assertThat(passNode.get("status").asText()).isEqualTo("ISSUED");
        assertThat(qrCode).startsWith("GP-");

        // Query student gate passes
        JsonNode list = data(getAs("/gate-passes", parentToken, "tenantId", tenantId.toString(), "studentId", studentId.toString()).andExpect(status().isOk()));
        assertThat(list.size()).isGreaterThanOrEqualTo(1);

        // Security Guard scans QR code to authorize departure (US-09006)
        GatePassVerifyRequest verifyReq = GatePassVerifyRequest.builder()
                .qrCode(qrCode)
                .securityGuardName("Guard Ram Singh")
                .gateId("GATE_MAIN_01")
                .build();

        JsonNode verifiedNode = data(postJson("/gate-passes/verify", securityToken, verifyReq).andExpect(status().isOk()));
        assertThat(verifiedNode.get("status").asText()).isEqualTo("DEPARTED");
        assertThat(verifiedNode.get("securityVerifiedBy").asText()).isEqualTo("Guard Ram Singh");
    }

    @Test
    @DisplayName("US-09007: Register visitor appointments, check-in with digital badge, and check-out")
    void testVisitorAppointments() throws Exception {
        String receptionistToken = token("RECEPTIONIST");

        VisitorAppointmentRequest req = VisitorAppointmentRequest.builder()
                .tenantId(tenantId)
                .visitorName("Vikram Seth")
                .visitorPhone("+919123456789")
                .visitorIdType("AADHAAR")
                .visitorIdNumber("998877665544")
                .purpose("Parent Teacher Discussion regarding Olympiad")
                .hostStaffId(teacherUserId.toString())
                .hostStaffName("Dr. Kavya Nair")
                .appointmentTime(Instant.now().plusSeconds(1800))
                .build();

        JsonNode vNode = data(postJson("/visitors/appointments", receptionistToken, req).andExpect(status().isOk()));
        UUID appointmentId = UUID.fromString(vNode.get("appointmentId").asText());
        assertThat(vNode.get("badgeNumber").asText()).startsWith("VIS-");
        assertThat(vNode.get("status").asText()).isEqualTo("SCHEDULED");

        // Check in visitor
        VisitorCheckInRequest checkInReq = VisitorCheckInRequest.builder()
                .badgeNumber(vNode.get("badgeNumber").asText())
                .build();
        JsonNode checkedIn = data(putJson("/visitors/appointments/" + appointmentId + "/check-in", receptionistToken, checkInReq).andExpect(status().isOk()));
        assertThat(checkedIn.get("status").asText()).isEqualTo("CHECKED_IN");

        // Check out visitor
        JsonNode checkedOut = data(putJson("/visitors/appointments/" + appointmentId + "/check-out", receptionistToken, null).andExpect(status().isOk()));
        assertThat(checkedOut.get("status").asText()).isEqualTo("CHECKED_OUT");

        // Query appointments
        JsonNode list = data(getAs("/visitors/appointments", receptionistToken, "tenantId", tenantId.toString()).andExpect(status().isOk()));
        assertThat(list.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("US-09016: Manage appointment booking slots for principal meetings and book by parent")
    void testAppointmentSlots() throws Exception {
        String receptionistToken = token("RECEPTIONIST");
        String parentToken = token("PARENT");

        AppointmentSlotRequest slotReq = AppointmentSlotRequest.builder()
                .tenantId(tenantId)
                .hostUserId(UUID.randomUUID().toString())
                .hostName("Principal Dr. Sunita Rao")
                .slotDate(LocalDate.now().plusDays(2))
                .startTime("10:00")
                .endTime("10:30")
                .build();

        JsonNode slotNode = data(postJson("/visitors/slots", receptionistToken, slotReq).andExpect(status().isCreated()));
        UUID slotId = UUID.fromString(slotNode.get("slotId").asText());
        assertThat(slotNode.get("isBooked").asBoolean()).isFalse();

        // Book slot by parent
        BookSlotRequest bookReq = BookSlotRequest.builder()
                .parentId(parentUserId.toString())
                .parentName("Rajesh Sharma")
                .build();

        JsonNode booked = data(postJson("/visitors/slots/" + slotId + "/book", parentToken, bookReq).andExpect(status().isOk()));
        assertThat(booked.get("isBooked").asBoolean()).isTrue();
        assertThat(booked.get("bookedByParentName").asText()).isEqualTo("Rajesh Sharma");

        // Query available slots
        JsonNode slots = data(getAs("/visitors/slots", parentToken, "tenantId", tenantId.toString(), "date", LocalDate.now().plusDays(2).toString()).andExpect(status().isOk()));
        assertThat(slots.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("US-09017: Audit security gate entry and exit logs for unauthorized visitor attempts")
    void testGateAuditLogs() throws Exception {
        String securityToken = token("SECURITY_GUARD");

        GateAuditLogRequest logReq = GateAuditLogRequest.builder()
                .tenantId(tenantId)
                .gateId("GATE_NORTH_02")
                .gateName("North Delivery Gate")
                .passType("VISITOR_PASS")
                .personName("Unknown Visitor")
                .eventType("UNAUTHORIZED_ATTEMPT")
                .outcome("DENIED")
                .notes("Attempted entry without visitor appointment or ID document")
                .build();

        JsonNode logNode = data(postJson("/gate-passes/audit-logs", securityToken, logReq).andExpect(status().isCreated()));
        assertThat(logNode.get("outcome").asText()).isEqualTo("DENIED");
        assertThat(logNode.get("eventType").asText()).isEqualTo("UNAUTHORIZED_ATTEMPT");

        // Query audit logs
        JsonNode logs = data(getAs("/gate-passes/audit-logs", securityToken, "tenantId", tenantId.toString()).andExpect(status().isOk()));
        assertThat(logs.size()).isGreaterThanOrEqualTo(1);
    }
}
