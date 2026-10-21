package com.kvs.communication.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.kvs.communication.dto.*;
import com.kvs.communication.enums.BroadcastPriority;
import com.kvs.communication.enums.ChannelType;
import com.kvs.communication.enums.DeliveryStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BroadcastControllerIntegrationTest extends CommunicationTestSupport {

    @Test
    @DisplayName("US-09000: Broadcast circulars simultaneously via WhatsApp, SMS, and App Push")
    void testCreateBroadcast() throws Exception {
        String proToken = token("PUBLIC_RELATIONS_OFFICER");

        BroadcastRequest req = BroadcastRequest.builder()
                .tenantId(tenantId)
                .senderUserId(UUID.randomUUID())
                .senderName("Public Relations Officer")
                .title("Annual Sports Day Circular 2026")
                .messageBody("All students and parents are cordially invited to the 2026 Annual Sports Meet.")
                .targetAudience("ALL_PARENTS")
                .channel(ChannelType.MULTI_CHANNEL)
                .priority(BroadcastPriority.NORMAL)
                .build();

        JsonNode node = data(postJson("/broadcasts", proToken, req).andExpect(status().isCreated()));
        UUID broadcastId = UUID.fromString(node.get("broadcastId").asText());
        assertThat(node.get("title").asText()).isEqualTo("Annual Sports Day Circular 2026");
        assertThat(node.get("channel").asText()).isEqualTo("MULTI_CHANNEL");

        // Verify broadcast can be queried
        JsonNode list = data(getAs("/broadcasts", proToken, "tenantId", tenantId.toString()).andExpect(status().isOk()));
        assertThat(list.size()).isGreaterThanOrEqualTo(1);

        // Verify single broadcast query
        JsonNode single = data(getAs("/broadcasts/" + broadcastId, proToken).andExpect(status().isOk()));
        assertThat(single.get("title").asText()).isEqualTo("Annual Sports Day Circular 2026");
    }

    @Test
    @DisplayName("US-09010: Emergency weather closure announcements dispatched to all stakeholders")
    void testEmergencyWeatherClosure() throws Exception {
        String principalToken = token("PRINCIPAL");

        EmergencyAlertRequest req = EmergencyAlertRequest.builder()
                .tenantId(tenantId)
                .senderUserId(UUID.randomUUID())
                .senderName("Principal Office")
                .title("EMERGENCY: Heavy Rainfall School Closure Alert")
                .messageBody("Due to severe weather warnings, campus is closed today. Online classes will resume tomorrow.")
                .targetAudience("ALL_STAKEHOLDERS")
                .build();

        JsonNode alertNode = data(postJson("/broadcasts/emergency", principalToken, req).andExpect(status().isCreated()));
        assertThat(alertNode.get("priority").asText()).isEqualTo("EMERGENCY");
        assertThat(alertNode.get("isPinned").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("US-09011: Track delivery status and trigger SMS fallback on WhatsApp delivery failure")
    void testDeliveryStatusFallback() throws Exception {
        String proToken = token("PUBLIC_RELATIONS_OFFICER");
        String sysToken = token("SYSTEM_AUTOMATION");

        // Create broadcast first
        BroadcastRequest req = BroadcastRequest.builder()
                .tenantId(tenantId)
                .senderUserId(UUID.randomUUID())
                .title("Bus Route Revision Circular")
                .messageBody("Bus Route 14 pickup schedule revised by 10 minutes.")
                .targetAudience("ROUTE_14_PARENTS")
                .channel(ChannelType.WHATSAPP)
                .build();

        JsonNode bNode = data(postJson("/broadcasts", proToken, req).andExpect(status().isCreated()));
        UUID broadcastId = UUID.fromString(bNode.get("broadcastId").asText());

        // Get delivery logs
        JsonNode logs = data(getAs("/broadcasts/" + broadcastId + "/delivery-logs", proToken).andExpect(status().isOk()));
        assertThat(logs.size()).isGreaterThanOrEqualTo(1);
        UUID deliveryId = UUID.fromString(logs.get(0).get("deliveryId").asText());

        // Simulate WhatsApp delivery failure to trigger SMS fallback
        DeliveryLogUpdateRequest updateReq = DeliveryLogUpdateRequest.builder()
                .deliveryStatus(DeliveryStatus.FAILED)
                .errorMessage("WhatsApp Gateway Unreachable: HTTP 503")
                .build();

        JsonNode updatedLog = data(putJson("/broadcasts/delivery/" + deliveryId, sysToken, updateReq).andExpect(status().isOk()));
        assertThat(updatedLog.get("fallbackTriggered").asBoolean()).isTrue();
        assertThat(updatedLog.get("channel").asText()).isEqualTo("SMS");
    }

    @Test
    @DisplayName("US-09018: Pin urgent announcements to top of mobile feeds")
    void testPinBroadcast() throws Exception {
        String proToken = token("PUBLIC_RELATIONS_OFFICER");
        String principalToken = token("PRINCIPAL");

        BroadcastRequest req = BroadcastRequest.builder()
                .tenantId(tenantId)
                .senderUserId(UUID.randomUUID())
                .title("Exam Admit Card Dispatch")
                .messageBody("Download term 1 admit cards from portal.")
                .targetAudience("GRADE_10")
                .channel(ChannelType.PUSH)
                .build();

        JsonNode bNode = data(postJson("/broadcasts", proToken, req).andExpect(status().isCreated()));
        UUID broadcastId = UUID.fromString(bNode.get("broadcastId").asText());

        PinBroadcastRequest pinReq = PinBroadcastRequest.builder()
                .isPinned(true)
                .pinnedUntil(Instant.now().plusSeconds(3600))
                .build();

        JsonNode pinned = data(putJson("/broadcasts/" + broadcastId + "/pin", principalToken, pinReq).andExpect(status().isOk()));
        assertThat(pinned.get("isPinned").asBoolean()).isTrue();

        JsonNode pinnedList = data(getAs("/broadcasts/pinned", proToken, "tenantId", tenantId.toString()).andExpect(status().isOk()));
        assertThat(pinnedList.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("US-09019: Archive past communication broadcasts for regulatory compliance")
    void testArchiveBroadcast() throws Exception {
        String proToken = token("PUBLIC_RELATIONS_OFFICER");

        BroadcastRequest req = BroadcastRequest.builder()
                .tenantId(tenantId)
                .senderUserId(UUID.randomUUID())
                .title("Past Academic Session Circular 2024")
                .messageBody("Archived record.")
                .targetAudience("ALL")
                .channel(ChannelType.SMS)
                .build();

        JsonNode bNode = data(postJson("/broadcasts", proToken, req).andExpect(status().isCreated()));
        UUID broadcastId = UUID.fromString(bNode.get("broadcastId").asText());

        ArchiveBroadcastRequest archiveReq = ArchiveBroadcastRequest.builder()
                .isArchived(true)
                .build();

        JsonNode archived = data(putJson("/broadcasts/" + broadcastId + "/archive", proToken, archiveReq).andExpect(status().isOk()));
        assertThat(archived.get("isArchived").asBoolean()).isTrue();
        assertThat(archived.get("status").asText()).isEqualTo("ARCHIVED");

        JsonNode archiveList = data(getAs("/broadcasts/archived", proToken, "tenantId", tenantId.toString()).andExpect(status().isOk()));
        assertThat(archiveList.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Security & Validation: 400 on invalid payload, 401 unauthenticated, 403 forbidden role")
    void testSecurityAndValidation() throws Exception {
        String proToken = token("PUBLIC_RELATIONS_OFFICER");
        String parentToken = token("PARENT");

        // 400 Bad Request on empty title
        BroadcastRequest invalidReq = BroadcastRequest.builder()
                .tenantId(tenantId)
                .senderUserId(UUID.randomUUID())
                .title("") // Blank title fails validation
                .messageBody("Some body")
                .targetAudience("ALL")
                .channel(ChannelType.SMS)
                .build();
        postJson("/broadcasts", proToken, invalidReq).andExpect(status().isBadRequest());

        // 401 Unauthorized without token
        mockMvc.perform(get(BASE + "/broadcasts?tenantId=" + tenantId))
                .andExpect(status().isUnauthorized());

        // 403 Forbidden: parent trying to create broadcast circular
        BroadcastRequest validReq = BroadcastRequest.builder()
                .tenantId(tenantId)
                .senderUserId(UUID.randomUUID())
                .title("Valid Circular")
                .messageBody("Valid Body")
                .targetAudience("ALL")
                .channel(ChannelType.SMS)
                .build();
        postJson("/broadcasts", parentToken, validReq).andExpect(status().isForbidden());
    }
}
