package com.kvs.communication.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.kvs.communication.dto.DocumentAccessRequest;
import com.kvs.communication.dto.DocumentActivityRequest;
import com.kvs.communication.dto.DocumentFolderRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentLibraryIntegrationTest extends CommunicationTestSupport {

    @Test
    @DisplayName("Documents: create a folder, upload a file, change who can view it and read its access history")
    void testDocumentLibrary() throws Exception {
        String admin = token("SUPER_ADMIN", "u1111111-1111-1111-1111-111111111111");
        String teacher = token("TEACHER");

        DocumentFolderRequest folderReq = DocumentFolderRequest.builder()
                .tenantId(tenantId).name("School policies").defaultAccess("ALL_STAFF").build();
        postJson("/documents/folders", teacher, folderReq).andExpect(status().isForbidden());
        JsonNode folder = data(postJson("/documents/folders", admin, folderReq).andExpect(status().isCreated()));
        String folderId = folder.get("folderId").asText();
        postJson("/documents/folders", admin, folderReq).andExpect(status().isConflict());

        // Folder settings: new uploads are visible to staff and parents
        folderReq.setDefaultAccess("STAFF_AND_PARENTS");
        assertThat(data(putJson("/documents/folders/" + folderId, admin, folderReq).andExpect(status().isOk()))
                .get("defaultAccess").asText()).isEqualTo("STAFF_AND_PARENTS");

        byte[] pdf = "%PDF-1.4 safeguarding".getBytes(StandardCharsets.US_ASCII);
        JsonNode doc = data(mockMvc.perform(multipart(BASE + "/documents/folders/" + folderId + "/files")
                        .file(new MockMultipartFile("file", "Safeguarding policy 2026.pdf", "application/pdf", pdf))
                        .param("ownerUserId", "u1111111-1111-1111-1111-111111111111")
                        .param("ownerName", "Helen Morris")
                        .header("Authorization", admin))
                .andExpect(status().isCreated()));
        String docId = doc.get("documentId").asText();
        assertThat(doc.get("accessLevel").asText()).isEqualTo("STAFF_AND_PARENTS");
        assertThat(doc.get("sizeBytes").asLong()).isEqualTo(pdf.length);
        assertThat(doc.get("hasContent").asBoolean()).isTrue();

        mockMvc.perform(get(BASE + "/documents/" + docId + "/content").header("Authorization", teacher))
                .andExpect(status().isOk()).andExpect(content().bytes(pdf));

        JsonNode list = data(getAs("/documents", teacher, "tenantId", tenantId.toString(), "folderId", folderId).andExpect(status().isOk()));
        assertThat(list.size()).isEqualTo(1);
        JsonNode folders = data(getAs("/documents/folders", teacher, "tenantId", tenantId.toString()).andExpect(status().isOk()));
        assertThat(folders.get(0).get("documentCount").asLong()).isEqualTo(1);

        putJson("/documents/" + docId + "/access", admin, new DocumentAccessRequest("BOGUS", "Rajesh Admin")).andExpect(status().isBadRequest());
        assertThat(data(putJson("/documents/" + docId + "/access", admin, new DocumentAccessRequest("ALL_STAFF", "Rajesh Admin"))
                .andExpect(status().isOk())).get("accessLevel").asText()).isEqualTo("ALL_STAFF");

        postJson("/documents/" + docId + "/access-log", teacher, new DocumentActivityRequest("Kavya Nair", "Share link copied", true))
                .andExpect(status().isCreated());
        JsonNode history = data(getAs("/documents/" + docId + "/access-log", teacher).andExpect(status().isOk()));
        assertThat(history.size()).isEqualTo(3);
        assertThat(history.get(0).get("action").asText()).isEqualTo("Share link copied");
        assertThat(data(getAs("/documents", teacher, "tenantId", tenantId.toString())).get(0).get("isShared").asBoolean()).isTrue();
    }
}
