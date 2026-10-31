package com.kvs.communication.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.kvs.communication.entity.BroadcastDeliveryLog;
import com.kvs.communication.entity.CommunicationBroadcast;
import com.kvs.communication.enums.ChannelType;
import com.kvs.communication.enums.DeliveryStatus;
import com.kvs.communication.repo.BroadcastDeliveryLogRepository;
import com.kvs.core.exception.BadRequestException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Delivers circulars and emergency alerts (US-09000, US-09010, US-09011) through the school's own providers
 * (Settings → Integrations, sent by authservice).
 * <ol>
 *   <li>The audience becomes recipients: one primary guardian per family (studentservice, siblings share one message) and/or
 *       active staff (hrservice).</li>
 *   <li>One delivery log row per recipient is saved as QUEUED with the circular, or SKIPPED with the reason (no mobile number,
 *       no email, app push not available).</li>
 *   <li>After the circular is saved, the queued rows are sent in the background in batches of 100. WhatsApp that fails or is
 *       not set up is sent again by SMS (fallback_triggered).</li>
 * </ol>
 */
@Service
@Slf4j
public class BroadcastDeliveryService {

    /** Someone to send to. label: "Parent of Aarav Sharma (Class 7)" or "Staff · Mathematics teacher" */
    public record Recipient(String name, String label, String phone, String email) { }

    private static final int BATCH = 100;
    private static final String NO_NUMBER = "No valid mobile number";

    @Autowired private ServiceClients clients;
    @Autowired private BroadcastDeliveryLogRepository logs;

    private final ExecutorService pool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "circular-delivery");
        t.setDaemon(true);
        return t;
    });

    @PreDestroy
    void stop() { pool.shutdown(); }

    /* ---------------- audience ---------------- */

    /** ALL_PARENTS, GRADE_<n> (class families), ALL_TEACHERS (all staff), ALL_STAKEHOLDERS (families and staff) */
    public List<Recipient> recipients(String bearer, String tenantId, String audience) {
        String a = audience == null ? "" : audience.trim().toUpperCase(Locale.ROOT);
        boolean families = a.equals("ALL_PARENTS") || a.equals("ALL_STAKEHOLDERS") || a.startsWith("GRADE_");
        boolean staff = a.equals("ALL_TEACHERS") || a.equals("ALL_STAKEHOLDERS");
        List<Recipient> out = new ArrayList<>();
        try {
            if (families) out.addAll(families(clients.guardianContacts(bearer, tenantId, a.startsWith("GRADE_") ? gradeCode(a) : null)));
            if (staff) {
                for (JsonNode s : clients.staffContacts(bearer, tenantId)) {
                    out.add(new Recipient(text(s, "name"), "Staff · " + Objects.requireNonNullElse(text(s, "designation"), "staff"), text(s, "phone"), text(s, "email")));
                }
            }
        } catch (RestClientResponseException e) {
            throw new BadRequestException("Could not load the recipients (" + (families ? "student records" : "staff records") + " answered "
                    + e.getStatusCode().value() + "). Check that you may send circulars to this audience.");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Could not load the recipients: student or staff records are not reachable right now. Try again.");
        }
        return out;
    }

    /** GRADE_10 → GRADE-10, GRADE_7 → GRADE-07, GRADE_LKG → GRADE-LKG */
    private static String gradeCode(String audience) {
        String g = audience.substring("GRADE_".length());
        return "GRADE-" + (g.matches("\\d") ? "0" + g : g);
    }

    /** One recipient per guardian phone (or email): brothers and sisters share one message */
    private static List<Recipient> families(JsonNode contacts) {
        Map<String, List<JsonNode>> byKey = new LinkedHashMap<>();
        for (JsonNode c : contacts) {
            String phone = digits(text(c, "phone"));
            String key = phone != null ? phone : text(c, "email") != null ? text(c, "email").toLowerCase(Locale.ROOT) : "student:" + text(c, "studentId");
            byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
        }
        List<Recipient> out = new ArrayList<>();
        for (List<JsonNode> group : byKey.values()) {
            JsonNode first = group.get(0);
            String children = String.join(", ", group.stream().map(c -> text(c, "studentName") + (text(c, "className") != null ? " (" + text(c, "className") + ")" : "")).toList());
            String who = text(first, "relationship") != null ? capitalise(text(first, "relationship")) : "Guardian";
            out.add(new Recipient(Objects.requireNonNullElse(text(first, "guardianName"), "No guardian recorded"), who + " of " + children,
                    text(first, "phone"), text(first, "email")));
        }
        return out;
    }

    /* ---------------- queue and send ---------------- */

    /**
     * Saves one delivery row per recipient (QUEUED, or SKIPPED with the reason) and, once the circular's transaction commits,
     * sends the queued rows in the background. Call inside the transaction that saves the circular.
     */
    public void queue(CommunicationBroadcast b, String bearer) {
        if (bearer == null) throw new BadRequestException("Sign in again to send this circular");
        List<Recipient> people = recipients(bearer, b.getTenantId(), b.getTargetAudience());
        ChannelType first = b.getChannel() == ChannelType.MULTI_CHANNEL ? ChannelType.WHATSAPP : b.getChannel();
        List<BroadcastDeliveryLog> rows = new ArrayList<>();
        for (Recipient r : people) {
            BroadcastDeliveryLog row = BroadcastDeliveryLog.builder()
                    .broadcastId(b.getBroadcastId()).recipientName(clip(r.name() + " · " + r.label(), 300))
                    .recipientPhone(Objects.requireNonNullElse(clip(r.phone(), 20), "")).recipientEmail(clip(r.email(), 100))
                    .channel(first).deliveryStatus(DeliveryStatus.QUEUED).fallbackTriggered(false).build();
            if (first == ChannelType.PUSH) skip(row, "App push is not available yet");
            else if (first == ChannelType.EMAIL && r.email() == null) skip(row, "No email address on record");
            else if (first != ChannelType.EMAIL && digits(r.phone()) == null) skip(row, "No mobile number on record");
            rows.add(row);
        }
        logs.saveAll(rows);
        UUID id = b.getBroadcastId();
        Runnable send = () -> pool.submit(() -> {
            try { send(id, b.getTenantId(), b.getTitle(), b.getMessageBody(), bearer); }
            catch (Exception e) { log.error("Delivery of circular {} stopped: {}", id, e.getMessage(), e); }
        });
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { send.run(); }
            });
        } else {
            send.run();
        }
    }

    /** Sends the circular's queued rows; WhatsApp that fails or is not set up goes again by SMS. */
    void send(UUID broadcastId, String tenantId, String title, String body, String bearer) {
        List<BroadcastDeliveryLog> queued = logs.findByBroadcastIdAndDeliveryStatus(broadcastId, DeliveryStatus.QUEUED);
        Map<ChannelType, List<BroadcastDeliveryLog>> byChannel = new EnumMap<>(ChannelType.class);
        queued.forEach(l -> byChannel.computeIfAbsent(l.getChannel(), k -> new ArrayList<>()).add(l));
        for (Map.Entry<ChannelType, List<BroadcastDeliveryLog>> e : byChannel.entrySet()) {
            deliver(e.getKey(), e.getValue(), broadcastId, tenantId, title, body, bearer);
        }
        // fallback: WhatsApp not accepted → SMS
        List<BroadcastDeliveryLog> again = new ArrayList<>();
        for (BroadcastDeliveryLog l : byChannel.getOrDefault(ChannelType.WHATSAPP, List.of())) {
            if (l.getDeliveryStatus() == DeliveryStatus.SENT || NO_NUMBER.equals(l.getErrorMessage())) continue;
            l.setFallbackTriggered(true);
            l.setChannel(ChannelType.SMS);
            l.setErrorMessage("WhatsApp: " + l.getErrorMessage());
            again.add(l);
        }
        if (!again.isEmpty()) deliver(ChannelType.SMS, again, broadcastId, tenantId, title, body, bearer);
        log.info("Circular {}: {} queued, {} sent again by SMS", broadcastId, queued.size(), again.size());
    }

    private void deliver(ChannelType channel, List<BroadcastDeliveryLog> rows, UUID broadcastId, String tenantId, String title, String body, String bearer) {
        for (int from = 0; from < rows.size(); from += BATCH) {
            List<BroadcastDeliveryLog> batch = rows.subList(from, Math.min(rows.size(), from + BATCH));
            List<Map<String, Object>> messages = new ArrayList<>();
            for (BroadcastDeliveryLog l : batch) {
                Map<String, Object> m = new HashMap<>();
                m.put("ref", l.getDeliveryId().toString());
                m.put("recipient", channel == ChannelType.EMAIL ? l.getRecipientEmail() : l.getRecipientPhone());
                m.put("subject", title);
                m.put("title", title);
                m.put("body", body);
                messages.add(m);
            }
            Map<String, JsonNode> results = new HashMap<>();
            String batchError = null;
            try {
                JsonNode reply = clients.deliver(bearer, tenantId, Map.of("channel", channel.name(), "reference", "circular " + broadcastId, "messages", messages));
                for (JsonNode r : reply.path("results")) results.put(r.path("ref").asText(), r);
            } catch (RestClientResponseException e) {
                batchError = "The messaging service refused the batch (HTTP " + e.getStatusCode().value() + ")";
            } catch (Exception e) {
                batchError = "The messaging service could not be reached";
            }
            for (BroadcastDeliveryLog l : batch) {
                JsonNode r = results.get(l.getDeliveryId().toString());
                String prior = Boolean.TRUE.equals(l.getFallbackTriggered()) ? l.getErrorMessage() : null;
                if (r == null) {
                    l.setDeliveryStatus(DeliveryStatus.FAILED);
                    l.setErrorMessage(join(prior, batchError != null ? batchError : "No result returned"));
                } else {
                    String status = r.path("status").asText("FAILED");
                    l.setDeliveryStatus(switch (status) { case "SENT" -> DeliveryStatus.SENT; case "SKIPPED" -> DeliveryStatus.SKIPPED; default -> DeliveryStatus.FAILED; });
                    l.setGatewayResponseId(clip(text(r, "providerId"), 100));
                    String err = text(r, "error");
                    l.setErrorMessage(status.equals("SENT") ? prior : (prior == null ? err : join(prior, "SMS: " + err)));
                }
            }
            // saveAll returns the stored copies (new version numbers): keep those, so the SMS fallback saves them again cleanly
            List<BroadcastDeliveryLog> stored = logs.saveAll(batch);
            for (int k = 0; k < stored.size(); k++) batch.set(k, stored.get(k));
        }
    }

    private static void skip(BroadcastDeliveryLog row, String why) {
        row.setDeliveryStatus(DeliveryStatus.SKIPPED);
        row.setErrorMessage(why);
    }

    /* ---------------- helpers ---------------- */

    private static String join(String a, String b) { return a == null ? b : b == null ? a : a + "; " + b; }
    private static String text(JsonNode n, String f) { JsonNode v = n.path(f); return v.isMissingNode() || v.isNull() || v.asText().isBlank() ? null : v.asText(); }
    private static String digits(String phone) { if (phone == null) return null; String d = phone.replaceAll("[^0-9]", ""); return d.length() >= 10 ? d : null; }
    private static String clip(String s, int n) { return s == null ? null : s.length() > n ? s.substring(0, n) : s; }
    private static String capitalise(String s) { String l = s.toLowerCase(Locale.ROOT).replace('_', ' '); return Character.toUpperCase(l.charAt(0)) + l.substring(1); }
}
