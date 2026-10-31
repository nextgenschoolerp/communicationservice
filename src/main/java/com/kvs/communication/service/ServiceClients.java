package com.kvs.communication.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

/**
 * Calls to the other services for circular delivery, made with the sender's own bearer token, so each service applies its
 * own role and school checks: studentservice (guardian contacts), hrservice (staff contacts) and authservice (the school's
 * email / SMS / WhatsApp providers, whose secrets never leave authservice).
 */
@Component
public class ServiceClients {

    private final RestClient http;
    private final ObjectMapper objectMapper;
    private final String authUrl;
    private final String studentUrl;
    private final String hrUrl;

    public ServiceClients(ObjectMapper objectMapper,
                          @Value("${communication.services.auth-url:http://localhost:8082/authservice}") String authUrl,
                          @Value("${communication.services.student-url:http://localhost:8084/studentservice}") String studentUrl,
                          @Value("${communication.services.hr-url:http://localhost:8088/hrservice}") String hrUrl) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5_000);
        // a batch of 100 messages is sent one by one by authservice
        f.setReadTimeout(600_000);
        this.http = RestClient.builder().requestFactory(f).build();
        this.objectMapper = objectMapper;
        this.authUrl = authUrl;
        this.studentUrl = studentUrl;
        this.hrUrl = hrUrl;
    }

    /** The caller's Authorization header, read while still on the request thread */
    public static String currentBearer() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a) {
            return a.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }

    public JsonNode guardianContacts(String bearer, String tenantId, String gradeCode) {
        return get(bearer, studentUrl + "/api/v1/students/guardian-contacts?tenantId={t}" + (gradeCode != null ? "&gradeCode={g}" : ""),
                gradeCode != null ? new Object[]{tenantId, gradeCode} : new Object[]{tenantId});
    }

    public JsonNode staffContacts(String bearer, String tenantId) {
        return get(bearer, hrUrl + "/api/v1/hr/staff-contacts?tenantId={t}", tenantId);
    }

    public JsonNode channelReadiness(String bearer, String tenantId) {
        return get(bearer, authUrl + "/api/v1/tenants/{t}/integrations/readiness", tenantId);
    }

    /** body: {channel, reference, messages: [{ref, recipient, subject, title, body}]}; returns {channel, provider, results: [...]} */
    public JsonNode deliver(String bearer, String tenantId, Map<String, Object> body) {
        String json = http.post().uri(authUrl + "/api/v1/tenants/{t}/integrations/deliver", tenantId)
                .header(HttpHeaders.AUTHORIZATION, bearer).contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().body(String.class);
        return data(json);
    }

    private JsonNode get(String bearer, String uri, Object... vars) {
        String json = http.get().uri(uri, vars).header(HttpHeaders.AUTHORIZATION, bearer).retrieve().body(String.class);
        return data(json);
    }

    private JsonNode data(String json) {
        try { return objectMapper.readTree(json == null ? "{}" : json).path("data"); }
        catch (Exception e) { throw new IllegalStateException("Unreadable reply: " + e.getMessage(), e); }
    }
}
