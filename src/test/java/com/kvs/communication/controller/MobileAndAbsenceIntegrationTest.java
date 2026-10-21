package com.kvs.communication.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.kvs.communication.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MobileAndAbsenceIntegrationTest extends CommunicationTestSupport {

    @Test
    @DisplayName("US-09001: Dispatch automated alerts within 30 minutes of unexcused student absence and escalate")
    void testAbsenceAlertDispatchAndEscalation() throws Exception {
        String sysToken = token("SYSTEM_AUTOMATION");

        AbsenceAlertRequest req = AbsenceAlertRequest.builder()
                .tenantId(tenantId)
                .studentId(studentId)
                .studentName("Aarav Sharma")
                .guardianPhone("+919876543210")
                .guardianName("Rajesh Sharma")
                .absenceDate(LocalDate.now())
                .alertChannel("WHATSAPP")
                .build();

        JsonNode alertNode = data(postJson("/absence-alerts", sysToken, req).andExpect(status().isCreated()));
        UUID alertId = UUID.fromString(alertNode.get("alertId").asText());
        assertThat(alertNode.get("studentName").asText()).isEqualTo("Aarav Sharma");
        assertThat(alertNode.get("escalated").asBoolean()).isFalse();

        // Escalate alert
        JsonNode escalated = data(postJson("/absence-alerts/" + alertId + "/escalate", sysToken, null).andExpect(status().isOk()));
        assertThat(escalated.get("escalated").asBoolean()).isTrue();

        // Query absence alerts
        JsonNode list = data(getAs("/absence-alerts", sysToken, "tenantId", tenantId.toString(), "date", LocalDate.now().toString()).andExpect(status().isOk()));
        assertThat(list.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("US-09002: Register mobile app push notification device tokens")
    void testRegisterDeviceToken() throws Exception {
        String parentToken = token("PARENT");

        DeviceRegistrationRequest req = DeviceRegistrationRequest.builder()
                .tenantId(tenantId)
                .userId(parentUserId)
                .deviceToken("fcm-device-token-xyz-123456789")
                .platform("ANDROID")
                .appVersion("2.4.1")
                .build();

        JsonNode regNode = data(postJson("/mobile/devices", parentToken, req).andExpect(status().isCreated()));
        assertThat(regNode.get("platform").asText()).isEqualTo("ANDROID");
        assertThat(regNode.get("isActive").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("US-09003: Multi-child profile switching under single parent credentials")
    void testMultiChildProfileSwitching() throws Exception {
        String parentToken = token("PARENT");

        // Map child 1
        ParentChildMappingRequest child1 = ParentChildMappingRequest.builder()
                .tenantId(tenantId)
                .parentUserId(parentUserId)
                .parentPhone("+919876543210")
                .parentName("Rajesh Sharma")
                .studentId(studentId)
                .studentName("Aarav Sharma")
                .grade("10")
                .section("A")
                .isPrimary(true)
                .relationship("FATHER")
                .build();
        data(postJson("/mobile/child-mappings", parentToken, child1).andExpect(status().isCreated()));

        // Map child 2 (sibling)
        UUID siblingId = UUID.randomUUID();
        ParentChildMappingRequest child2 = ParentChildMappingRequest.builder()
                .tenantId(tenantId)
                .parentUserId(parentUserId)
                .parentPhone("+919876543210")
                .parentName("Rajesh Sharma")
                .studentId(siblingId)
                .studentName("Ananya Sharma")
                .grade("7")
                .section("B")
                .isPrimary(false)
                .relationship("FATHER")
                .build();
        data(postJson("/mobile/child-mappings", parentToken, child2).andExpect(status().isCreated()));

        // Query linked children for profile switching
        JsonNode children = data(getAs("/mobile/parents/" + parentUserId + "/children", parentToken).andExpect(status().isOk()));
        assertThat(children.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("US-09012: Configure notification preferences for parents to manage alert channels")
    void testNotificationPreferences() throws Exception {
        String parentToken = token("PARENT");

        NotificationPreferenceRequest req = NotificationPreferenceRequest.builder()
                .tenantId(tenantId)
                .userId(parentUserId)
                .whatsappEnabled(true)
                .smsEnabled(false)
                .emailEnabled(true)
                .pushEnabled(true)
                .quietHoursStart("23:00")
                .quietHoursEnd("06:30")
                .build();

        JsonNode prefNode = data(postJson("/mobile/preferences", parentToken, req).andExpect(status().isOk()));
        assertThat(prefNode.get("smsEnabled").asBoolean()).isFalse();
        assertThat(prefNode.get("quietHoursStart").asText()).isEqualTo("23:00");

        // Query preferences
        JsonNode retrieved = data(getAs("/mobile/preferences", parentToken, "tenantId", tenantId.toString(), "userId", parentUserId.toString()).andExpect(status().isOk()));
        assertThat(retrieved.get("whatsappEnabled").asBoolean()).isTrue();
    }
}
