package com.kvs.communication.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvs.communication.service.ServiceClients;
import com.kvs.core.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class CommunicationTestSupport {

    static final String BASE = "/api/v1/communication";

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected JwtTokenService jwtTokenService;
    @MockBean protected ServiceClients serviceClients;

    protected final String run = UUID.randomUUID().toString().substring(0, 8);
    protected final String tenantId = "T-COMM-" + run;
    protected final String tenant = tenantId;
    protected final UUID parentUserId = UUID.randomUUID();
    protected final UUID teacherUserId = UUID.randomUUID();
    protected final UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUpMockClients() throws Exception {
        JsonNode mockGuardians = objectMapper.readTree("""
            [
              {
                "studentId": "%s",
                "studentName": "Aarav Sharma",
                "className": "Class 10",
                "name": "Rajesh Sharma",
                "phone": "+919876543210",
                "email": "rajesh.sharma@example.com",
                "relationship": "Father"
              }
            ]
            """.formatted(studentId));
        JsonNode mockStaff = objectMapper.readTree("""
            [
              {
                "name": "Dr. Kavya Nair",
                "designation": "Mathematics Teacher",
                "phone": "+919876543211",
                "email": "kavya.nair@example.com"
              }
            ]
            """);
        JsonNode mockReadiness = objectMapper.readTree("{\"whatsapp\": true, \"sms\": true, \"email\": true}");
        JsonNode mockDeliver = objectMapper.readTree("{\"channel\": \"WHATSAPP\", \"results\": []}");

        when(serviceClients.guardianContacts(any(), any(), any())).thenReturn(mockGuardians);
        when(serviceClients.staffContacts(any(), any())).thenReturn(mockStaff);
        when(serviceClients.channelReadiness(any(), any())).thenReturn(mockReadiness);
        when(serviceClients.deliver(any(), any(), any())).thenReturn(mockDeliver);
    }

    protected String token(String role) {
        return token(role, role.toLowerCase(Locale.ROOT) + "-" + run);
    }

    protected String token(String role, String userId) {
        return token(role, userId, tenant);
    }

    protected String token(String role, String userId, String tenantStr) {
        return "Bearer " + jwtTokenService.generateToken(userId + "@schoolerp.internal",
                Map.of("roles", List.of("ROLE_" + role), "tenantId", tenantStr, "userId", userId));
    }

    protected ResultActions postJson(String path, String token, Object body) throws Exception {
        return mockMvc.perform(withBody(post(BASE + path), token, body));
    }

    protected ResultActions putJson(String path, String token, Object body) throws Exception {
        return mockMvc.perform(withBody(put(BASE + path), token, body));
    }

    protected ResultActions getAs(String path, String token, String... params) throws Exception {
        MockHttpServletRequestBuilder req = get(BASE + path);
        if (token != null) {
            req.header("Authorization", token);
        }
        for (int i = 0; i < params.length; i += 2) {
            req.param(params[i], params[i + 1]);
        }
        return mockMvc.perform(req);
    }

    protected MockHttpServletRequestBuilder withBody(MockHttpServletRequestBuilder builder, String token, Object body) throws Exception {
        if (token != null) {
            builder.header("Authorization", token);
        }
        builder.contentType(MediaType.APPLICATION_JSON);
        if (body != null) {
            builder.content(objectMapper.writeValueAsString(body));
        }
        return builder;
    }

    protected JsonNode data(ResultActions actions) throws Exception {
        byte[] bytes = actions.andReturn().getResponse().getContentAsByteArray();
        JsonNode root = objectMapper.readTree(bytes);
        return root.has("data") ? root.get("data") : root;
    }
}
