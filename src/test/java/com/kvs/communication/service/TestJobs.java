package com.kvs.communication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvs.core.messaging.JobContext;
import com.kvs.core.messaging.JobHandler;

import java.util.UUID;

/** Runs a queued job's handler directly in a test, as the ms-messaging worker would (payload through JSON and back). */
public final class TestJobs {

    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    private TestJobs() { }

    public static void run(JobHandler handler, Object payload, String tenantId, int attempt) throws Exception {
        String text = JSON.writeValueAsString(payload);
        handler.run(new JobContext() {
            private final UUID id = UUID.randomUUID();
            @Override public UUID jobId() { return id; }
            @Override public String tenantId() { return tenantId; }
            @Override public String createdBy() { return "sender@example.com"; }
            @Override public int attempt() { return attempt; }
            @Override public <T> T payload(Class<T> type) {
                try { return JSON.readValue(text, type); } catch (Exception e) { throw new IllegalStateException(e); }
            }
            @Override public void progress(int done, int total) { }
            @Override public void result(Object result) { }
        });
    }
}
