package com.kvs.communication.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.kvs.communication.dto.ParentChildMappingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /mobile/parents/{parentId}/wards: a parent may read only their own wards; staff may read any parent's wards of
 * their own tenant. Tokens are minted with {@code jwtTokenService.generateToken}, as in the scenario access tests.
 */
class ParentWardsAccessTest extends CommunicationTestSupport {

    private final String otherParentId = UUID.randomUUID().toString();

    @BeforeEach
    void mapWard() throws Exception {
        postJson("/mobile/child-mappings", token("PARENT", parentUserId.toString()), ParentChildMappingRequest.builder()
                .tenantId(tenantId)
                .parentUserId(parentUserId.toString())
                .parentPhone("+919876543210")
                .parentName("Rajesh Sharma")
                .studentId(studentId)
                .studentName("Aarav Sharma")
                .grade("10")
                .section("A")
                .build())
                .andExpect(status().isCreated());
    }

    private String wards(String parentId) {
        return "/mobile/parents/" + parentId + "/wards";
    }

    @Test
    @DisplayName("A parent reading their own wards gets 200")
    void parentReadsOwnWards() throws Exception {
        JsonNode wards = data(getAs(wards(parentUserId.toString()), token("PARENT", parentUserId.toString()))
                .andExpect(status().isOk()));
        assertThat(wards.size()).isEqualTo(1);
        assertThat(wards.get(0).get("studentName").asText()).isEqualTo("Aarav Sharma");
    }

    @Test
    @DisplayName("A parent reading another parent's wards gets 403")
    void parentReadsOtherParentsWards() throws Exception {
        getAs(wards(parentUserId.toString()), token("PARENT", otherParentId)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("An applicant parent reading another parent's wards gets 403")
    void applicantParentReadsOtherParentsWards() throws Exception {
        getAs(wards(parentUserId.toString()), token("APPLICANT_PARENT", otherParentId)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A staff role reading a parent's wards gets 200")
    void staffReadsParentsWards() throws Exception {
        JsonNode wards = data(getAs(wards(parentUserId.toString()), token("ADMIN")).andExpect(status().isOk()));
        assertThat(wards.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Staff of another tenant reading a parent's wards gets 403")
    void staffOfOtherTenantGetsForbidden() throws Exception {
        getAs(wards(parentUserId.toString()), token("ADMIN", "admin-" + run, "T-OTHER-" + run))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A parent whose token names another tenant gets 403 for wards mapped in this tenant")
    void parentWithOtherTenantTokenGetsForbidden() throws Exception {
        getAs(wards(parentUserId.toString()), token("PARENT", parentUserId.toString(), "T-OTHER-" + run))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A role outside the endpoint's list gets 403")
    void teacherGetsForbidden() throws Exception {
        getAs(wards(parentUserId.toString()), token("TEACHER")).andExpect(status().isForbidden());
    }
}
