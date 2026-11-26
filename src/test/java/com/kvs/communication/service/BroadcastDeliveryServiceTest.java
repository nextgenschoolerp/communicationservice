package com.kvs.communication.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kvs.communication.entity.BroadcastDeliveryLog;
import com.kvs.communication.entity.CommunicationBroadcast;
import com.kvs.communication.enums.ChannelType;
import com.kvs.communication.repo.BroadcastDeliveryLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Phase 6 (i18n): each recipient gets exactly one message, in the language version they can have. Translated versions go
 * by email only; SMS and circulars without versions get the main text. The delivery runs on a pool thread, so the tests
 * wait for the provider calls.
 */
class BroadcastDeliveryServiceTest {

    private static final String GUARDIANS = """
            [ {"studentId":"s1","studentName":"Aarav","className":"7A","guardianName":"Asha","relationship":"mother",
               "phone":"+91 98100 55501","email":"asha@example.com","language":"hi"},
              {"studentId":"s2","studentName":"Ravi","className":"5B","guardianName":"Dev","relationship":"father",
               "phone":"9810055502","email":"dev@example.com","language":null} ]""";

    private final ObjectMapper json = new ObjectMapper();
    private final ServiceClients clients = mock(ServiceClients.class);
    private final BroadcastDeliveryLogRepository logs = mock(BroadcastDeliveryLogRepository.class);
    private final Map<UUID, BroadcastDeliveryLog> stored = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> sent = new CopyOnWriteArrayList<>();
    private final BroadcastDeliveryService service = new BroadcastDeliveryService();

    @BeforeEach
    void wire() throws Exception {
        ReflectionTestUtils.setField(service, "clients", clients);
        ReflectionTestUtils.setField(service, "logs", logs);
        when(logs.saveAll(anyList())).thenAnswer(inv -> {
            List<BroadcastDeliveryLog> rows = inv.getArgument(0);
            rows.forEach(r -> {
                if (r.getDeliveryId() == null) r.setDeliveryId(UUID.randomUUID());
                stored.put(r.getDeliveryId(), r);
            });
            return rows;
        });
        when(logs.findAllById(anyCollection())).thenAnswer(inv -> {
            Collection<UUID> ids = inv.getArgument(0);
            return ids.stream().map(stored::get).toList();
        });
        when(clients.deliver(any(), any(), anyMap())).thenAnswer(inv -> {
            Map<String, Object> body = inv.getArgument(2);
            ObjectNode reply = json.createObjectNode();
            ArrayNode results = reply.putArray("results");
            for (Object m : (List<?>) body.get("messages")) {
                @SuppressWarnings("unchecked") Map<String, Object> message = (Map<String, Object>) m;
                sent.add(message);
                ObjectNode r = results.addObject();
                r.put("ref", (String) message.get("ref"));
                r.put("status", "SENT");
            }
            return reply;
        });
        when(clients.guardianContacts(any(), any(), any())).thenReturn(json.readTree(GUARDIANS));
    }

    private static String versions() {
        return CircularTranslations.toJson(CircularTranslations.validate(Map.of("hi",
                Map.of("title", "छुट्टी की सूचना", "body", "सोमवार को स्कूल बंद रहेगा।"))));
    }

    private CommunicationBroadcast circular(ChannelType channel, String translations) {
        return CommunicationBroadcast.builder().broadcastId(UUID.randomUUID()).tenantId("t1")
                .title("Holiday notice").messageBody("School is closed on Monday.").targetAudience("ALL_PARENTS")
                .channel(channel).translations(translations).build();
    }

    private Map<String, Object> messageTo(String recipient) {
        return sent.stream().filter(m -> recipient.equals(m.get("recipient"))).findFirst().orElseThrow();
    }

    @Test
    void emailToAGuardianWithAVersionGetsItAndTheOthersGetTheMainText() {
        service.queue(circular(ChannelType.EMAIL, versions()), "bearer");

        verify(clients, timeout(5_000).times(2)).deliver(any(), any(), anyMap());
        assertThat(sent).hasSize(2);
        assertThat(messageTo("asha@example.com").get("title")).isEqualTo("छुट्टी की सूचना");
        assertThat(messageTo("asha@example.com").get("body")).isEqualTo("सोमवार को स्कूल बंद रहेगा।");
        assertThat(messageTo("dev@example.com").get("title")).isEqualTo("Holiday notice");
        assertThat(messageTo("dev@example.com").get("body")).isEqualTo("School is closed on Monday.");
    }

    @Test
    void smsAlwaysGoesOutInTheMainTextBecauseItsTemplateIsInOneLanguage() {
        service.queue(circular(ChannelType.SMS, versions()), "bearer");

        verify(clients, timeout(5_000).times(1)).deliver(any(), any(), anyMap());
        assertThat(sent).hasSize(2);
        assertThat(sent).allSatisfy(m -> {
            assertThat(m.get("title")).isEqualTo("Holiday notice");
            assertThat(m.get("body")).isEqualTo("School is closed on Monday.");
        });
    }

    @Test
    void aCircularWithNoVersionsGoesOutInTheMainTextToEveryone() {
        service.queue(circular(ChannelType.EMAIL, null), "bearer");

        verify(clients, timeout(5_000).times(1)).deliver(any(), any(), anyMap());
        assertThat(sent).hasSize(2);
        assertThat(sent).allSatisfy(m -> assertThat(m.get("title")).isEqualTo("Holiday notice"));
    }

    @Test
    void eachRecipientIsDeliveredExactlyOnce() {
        service.queue(circular(ChannelType.EMAIL, versions()), "bearer");

        verify(clients, timeout(5_000).times(2)).deliver(any(), any(), anyMap());
        assertThat(sent).extracting(m -> m.get("ref")).doesNotHaveDuplicates().hasSize(2);
    }
}
