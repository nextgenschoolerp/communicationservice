package com.kvs.communication.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.kvs.communication.entity.BroadcastDeliveryLog;
import com.kvs.communication.entity.CommunicationBroadcast;
import com.kvs.communication.enums.ChannelType;
import com.kvs.communication.enums.DeliveryStatus;
import com.kvs.communication.repo.BroadcastDeliveryLogRepository;
import com.kvs.core.exception.BadRequestException;
import com.kvs.core.exception.ForbiddenException;
import com.kvs.core.messaging.JobContext;
import com.kvs.core.messaging.JobFailedException;
import com.kvs.core.messaging.JobHandler;
import com.kvs.core.messaging.JobQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;

/**
 * Delivers circulars and emergency alerts (US-09000, US-09010, US-09011) through the school's own providers
 * (Settings → Integrations, sent by authservice).
 * <ol>
 *   <li>The audience becomes recipients: one primary guardian per family (studentservice, siblings share one message) and/or
 *       active staff (hrservice).</li>
 *   <li>One delivery log row per recipient is saved as QUEUED with the circular, or SKIPPED with the reason (no mobile number,
 *       no email, app push not available).</li>
 *   <li>With the circular, one background job ({@value #JOB_TYPE}, ms-messaging) per batch of up to 100 queued rows is saved
 *       in the same transaction, so the sending survives a restart. The job sends its rows; WhatsApp that fails or is not
 *       set up is sent again by SMS (fallback_triggered). When the messaging service cannot be reached the rows stay QUEUED
 *       and the job is tried again later (messaging-queue-plan.md, Phase 1); on its last attempt they are FAILED.</li>
 * </ol>
 */
@Service
@Slf4j
public class BroadcastDeliveryService implements JobHandler {

    /** The background job that sends one batch of a circular's rows */
    public static final String JOB_TYPE = "comm.delivery";

    /** What the job needs: the text to send (the version chosen for these rows) and the rows */
    public record DeliveryJob(UUID broadcastId, String title, String body, List<UUID> deliveryIds) { }

    /**
     * Someone to send to. label: "Parent of Aarav Sharma (Class 7)" or "Staff · Mathematics teacher". language: the language
     * the guardian chose for messages, or null (staff have no language yet, and so has a guardian who chose none).
     */
    public record Recipient(String name, String label, String phone, String email, String language) { }

    private static final int BATCH = 100;
    private static final String NO_NUMBER = "No valid mobile number";

    @Autowired private ServiceClients clients;
    @Autowired private BroadcastDeliveryLogRepository logs;
    @Autowired private JobQueue jobs;

    /* ---------------- audience ---------------- */

    /** ALL_PARENTS, GRADE_<n> (class families), ALL_TEACHERS (all staff), ALL_STAKEHOLDERS (families and staff) */
    public List<Recipient> recipients(String bearer, String tenantId, String audience) {
        String a = audience == null ? "" : audience.trim().toUpperCase(Locale.ROOT);
        boolean families = a.equals("ALL_PARENTS") || a.equals("ALL_STAKEHOLDERS") || a.startsWith("GRADE_") || a.contains("PARENT");
        boolean staff = a.equals("ALL_TEACHERS") || a.equals("ALL_STAKEHOLDERS");
        List<Recipient> out = new ArrayList<>();
        try {
            if (families) out.addAll(families(clients.guardianContacts(bearer, tenantId, a.startsWith("GRADE_") ? gradeCode(a) : null)));
            if (staff) {
                for (JsonNode s : clients.staffContacts(bearer, tenantId)) {
                    out.add(new Recipient(text(s, "name"), "Staff · " + Objects.requireNonNullElse(text(s, "designation"), "staff"), text(s, "phone"), text(s, "email"), null));
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
            // siblings share one message: the first language any of them chose
            String language = group.stream().map(c -> text(c, "language")).filter(Objects::nonNull).findFirst().orElse(null);
            out.add(new Recipient(Objects.requireNonNullElse(text(first, "guardianName"), "No guardian recorded"), who + " of " + children,
                    text(first, "phone"), text(first, "email"), language));
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
        // Checked now by authservice, with the sender's token: the rows are sent later with the service's own identity,
        // because the sender's token may have expired by then (messaging-queue-plan.md, Phase 0)
        boolean allowed;
        try {
            allowed = clients.maySendCirculars(bearer, b.getTenantId());
        } catch (Exception e) {
            throw new BadRequestException("Could not check that you may send circulars (the sign-in service did not answer). Try again.");
        }
        if (!allowed) throw new ForbiddenException("You may not send circulars");
        List<Recipient> people = recipients(bearer, b.getTenantId(), b.getTargetAudience());
        ChannelType first = b.getChannel() == ChannelType.MULTI_CHANNEL ? ChannelType.WHATSAPP : b.getChannel();
        // Translated versions go out by email only: the SMS and WhatsApp templates are registered in one language
        Map<String, CircularTranslations.Version> versions = first == ChannelType.EMAIL
                ? CircularTranslations.fromJson(b.getTranslations()) : Map.of();
        List<BroadcastDeliveryLog> rows = new ArrayList<>();
        List<String> versionOf = new ArrayList<>();   // parallel to rows: the version each row is sent in ("" = the main text)
        for (Recipient r : people) {
            BroadcastDeliveryLog row = BroadcastDeliveryLog.builder()
                    .broadcastId(b.getBroadcastId()).recipientName(clip(r.name() + " · " + r.label(), 300))
                    .recipientPhone(Objects.requireNonNullElse(clip(r.phone(), 20), "")).recipientEmail(clip(r.email(), 100))
                    .channel(first).deliveryStatus(DeliveryStatus.QUEUED).fallbackTriggered(false).build();
            if (first == ChannelType.PUSH) skip(row, "App push is not available yet");
            else if (first == ChannelType.EMAIL && r.email() == null) skip(row, "No email address on record");
            else if (first != ChannelType.EMAIL && digits(r.phone()) == null) skip(row, "No mobile number on record");
            rows.add(row);
            versionOf.add(r.language() != null && versions.containsKey(r.language()) ? r.language() : "");
        }
        // saveAll returns the stored copies in the same order. Each language group is sent by its own task, with its own rows only.
        List<BroadcastDeliveryLog> stored = logs.saveAll(rows);
        Map<String, List<UUID>> groups = new LinkedHashMap<>();
        for (int k = 0; k < stored.size(); k++) {
            if (stored.get(k).getDeliveryStatus() != DeliveryStatus.QUEUED) continue;   // skipped rows have nothing to send
            groups.computeIfAbsent(versionOf.get(k), v -> new ArrayList<>()).add(stored.get(k).getDeliveryId());
        }
        CircularTranslations.Version main = new CircularTranslations.Version(b.getTitle(), b.getMessageBody());
        // queued in this transaction: the jobs exist exactly when the circular and its rows are saved
        groups.forEach((version, ids) -> {
            CircularTranslations.Version chosen = version.isEmpty() ? main : versions.get(version);
            for (int from = 0; from < ids.size(); from += BATCH) {
                List<UUID> part = List.copyOf(ids.subList(from, Math.min(ids.size(), from + BATCH)));
                jobs.enqueue(JOB_TYPE, new DeliveryJob(b.getBroadcastId(), chosen.title(), chosen.body(), part));
            }
        });
    }

    @Override
    public String type() { return JOB_TYPE; }

    /** Run by the job worker with the circular's school set. Safe to repeat: only rows still QUEUED are sent. */
    @Override
    public void run(JobContext job) {
        DeliveryJob d = job.payload(DeliveryJob.class);
        boolean lastAttempt = job.attempt() >= maxAttempts();
        int waiting = send(d.broadcastId(), job.tenantId(), d.deliveryIds(), d.title(), d.body(), lastAttempt);
        if (waiting > 0) {
            throw new JobFailedException("The messaging service could not be reached; " + waiting + " message(s) wait to be sent", true);
        }
    }

    /**
     * Sends the circular's queued rows; WhatsApp that fails or is not set up goes again by SMS. Returns how many rows were
     * left QUEUED because the messaging service could not be reached (always 0 on the last attempt: those are FAILED).
     */
    int send(UUID broadcastId, String tenantId, Collection<UUID> deliveryIds, String title, String body, boolean lastAttempt) {
        List<BroadcastDeliveryLog> queued = logs.findAllById(deliveryIds).stream()
                .filter(l -> l.getDeliveryStatus() == DeliveryStatus.QUEUED).toList();
        Map<ChannelType, List<BroadcastDeliveryLog>> byChannel = new EnumMap<>(ChannelType.class);
        queued.forEach(l -> byChannel.computeIfAbsent(l.getChannel(), k -> new ArrayList<>()).add(l));
        int waiting = 0;
        for (Map.Entry<ChannelType, List<BroadcastDeliveryLog>> e : byChannel.entrySet()) {
            waiting += deliver(e.getKey(), e.getValue(), broadcastId, tenantId, title, body, lastAttempt);
        }
        // fallback: WhatsApp not accepted → SMS
        List<BroadcastDeliveryLog> again = new ArrayList<>();
        for (BroadcastDeliveryLog l : byChannel.getOrDefault(ChannelType.WHATSAPP, List.of())) {
            if (l.getDeliveryStatus() == DeliveryStatus.SENT || l.getDeliveryStatus() == DeliveryStatus.QUEUED
                    || NO_NUMBER.equals(l.getErrorMessage())) continue;   // QUEUED: not tried yet (service unreachable)
            l.setFallbackTriggered(true);
            l.setChannel(ChannelType.SMS);
            l.setErrorMessage("WhatsApp: " + l.getErrorMessage());
            again.add(l);
        }
        if (!again.isEmpty()) waiting += deliver(ChannelType.SMS, again, broadcastId, tenantId, title, body, lastAttempt);
        log.info("Circular {}: {} queued, {} sent again by SMS, {} waiting", broadcastId, queued.size(), again.size(), waiting);
        return waiting;
    }

    /** Returns how many rows it left QUEUED (messaging service unreachable, not the last attempt) */
    private int deliver(ChannelType channel, List<BroadcastDeliveryLog> rows, UUID broadcastId, String tenantId, String title, String body,
                        boolean lastAttempt) {
        int waiting = 0;
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
            boolean unreachable = false;
            try {
                JsonNode reply = clients.deliver(tenantId, Map.of("channel", channel.name(), "reference", "circular " + broadcastId, "messages", messages));
                for (JsonNode r : reply.path("results")) results.put(r.path("ref").asText(), r);
            } catch (RestClientResponseException e) {
                batchError = "The messaging service refused the batch (HTTP " + e.getStatusCode().value() + ")";
            } catch (Exception e) {
                batchError = "The messaging service could not be reached";
                unreachable = true;
            }
            if (unreachable && !lastAttempt) {
                // left QUEUED (not saved): the job is tried again later and sends them then
                waiting += batch.size();
                continue;
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
        return waiting;
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
