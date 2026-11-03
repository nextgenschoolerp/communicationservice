package com.kvs.communication.controller;

import com.kvs.core.service.JwtTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.server.PathContainer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link AccessRuleController}: endpoint → allowed roles, read from the running service.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AccessRuleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private String bearer(String role) {
        return "Bearer " + jwtTokenService.generateToken("rules@kvs.com", Map.of("roles", List.of("ROLE_" + role)));
    }

    @Test
    @DisplayName("GET /api/v1/communication/access-rules - lists each endpoint with the roles its @PreAuthorize allows")
    void listsRules() throws Exception {
        mockMvc.perform(get("/api/v1/communication/access-rules").header("Authorization", bearer("PRINCIPAL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThan(0)));
    }

    @Test
    @DisplayName("GET /api/v1/communication/access-rules - 401 without a token")
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/communication/access-rules")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/communication/access-rules is answered only by the rules endpoint (no resource path shadows it)")
    void rulesPathIsNotShadowedByAResource() {
        PathContainer rules = PathContainer.parsePath("/api/v1/communication/access-rules");
        List<String> shadows = handlerMapping.getHandlerMethods().entrySet().stream()
                .filter(e -> e.getKey().getMethodsCondition().getMethods().isEmpty()
                        || e.getKey().getMethodsCondition().getMethods().contains(RequestMethod.GET))
                .filter(e -> !e.getValue().getBeanType().equals(AccessRuleController.class))
                .filter(e -> e.getKey().getPatternValues().stream()
                        .anyMatch(p -> PathPatternParser.defaultInstance.parse(p).matches(rules)))
                .map(e -> e.getKey().getPatternValues() + " -> " + e.getValue())
                .toList();
        assertThat(shadows, empty());
    }
}
