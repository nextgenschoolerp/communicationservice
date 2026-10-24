package com.kvs.communication.controller;

import com.kvs.core.dto.ApiResponse;
import com.kvs.communication.config.CommunicationRoles;
import com.kvs.communication.dto.*;
import com.kvs.communication.entity.SchoolDocument;
import com.kvs.communication.service.DocumentLibraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/** School document library behind the Documents page. */
@RestController
@RequestMapping("/api/v1/communication/documents")
@RequiredArgsConstructor
public class DocumentLibraryController {

    private final DocumentLibraryService documentService;

    @GetMapping("/folders")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<DocumentFolderResponse>>> listFolders(@RequestParam("tenantId") String tenantId) {
        return ok("Document folders retrieved successfully", documentService.listFolders(tenantId));
    }

    @PostMapping("/folders")
    @PreAuthorize(CommunicationRoles.DOCUMENT_MANAGE)
    public ResponseEntity<ApiResponse<DocumentFolderResponse>> createFolder(@Valid @RequestBody DocumentFolderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DocumentFolderResponse>builder().success(true)
                        .message("Folder created successfully").data(documentService.createFolder(request)).build());
    }

    @PutMapping("/folders/{folderId}")
    @PreAuthorize(CommunicationRoles.DOCUMENT_MANAGE)
    public ResponseEntity<ApiResponse<DocumentFolderResponse>> updateFolder(@PathVariable("folderId") UUID folderId,
                                                                           @Valid @RequestBody DocumentFolderRequest request) {
        return ok("Folder settings saved", documentService.updateFolder(folderId, request));
    }

    @GetMapping
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<SchoolDocumentResponse>>> listDocuments(@RequestParam("tenantId") String tenantId,
                                                                                  @RequestParam(name = "folderId", required = false) UUID folderId) {
        return ok("Documents retrieved successfully", documentService.listDocuments(tenantId, folderId));
    }

    @PostMapping(value = "/folders/{folderId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(CommunicationRoles.DOCUMENT_MANAGE)
    public ResponseEntity<ApiResponse<SchoolDocumentResponse>> upload(@PathVariable("folderId") UUID folderId,
                                                                      @RequestParam("file") MultipartFile file,
                                                                      @RequestParam(name = "ownerUserId", required = false) String ownerUserId,
                                                                      @RequestParam(name = "ownerName", required = false) String ownerName) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SchoolDocumentResponse>builder().success(true)
                        .message("Document uploaded successfully").data(documentService.upload(folderId, file, ownerUserId, ownerName)).build());
    }

    @GetMapping("/{documentId}/content")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<byte[]> download(@PathVariable("documentId") UUID documentId) {
        SchoolDocument doc = documentService.document(documentId);
        byte[] bytes = documentService.content(documentId);
        MediaType type = doc.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(doc.getContentType());
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(doc.getFileName(), StandardCharsets.UTF_8).build().toString())
                .body(bytes);
    }

    @PutMapping("/{documentId}/access")
    @PreAuthorize(CommunicationRoles.DOCUMENT_MANAGE)
    public ResponseEntity<ApiResponse<SchoolDocumentResponse>> changeAccess(@PathVariable("documentId") UUID documentId,
                                                                            @Valid @RequestBody DocumentAccessRequest request) {
        return ok("Permissions updated", documentService.changeAccess(documentId, request));
    }

    @PostMapping("/{documentId}/access-log")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<DocumentActivityResponse>> recordActivity(@PathVariable("documentId") UUID documentId,
                                                                               @Valid @RequestBody DocumentActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DocumentActivityResponse>builder().success(true)
                        .message("Activity recorded").data(documentService.recordActivity(documentId, request)).build());
    }

    @GetMapping("/{documentId}/access-log")
    @PreAuthorize(CommunicationRoles.ANY_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<DocumentActivityResponse>>> accessHistory(@PathVariable("documentId") UUID documentId) {
        return ok("Access history retrieved successfully", documentService.accessHistory(documentId));
    }

    private static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.<T>builder().success(true).message(message).data(data).build());
    }
}
