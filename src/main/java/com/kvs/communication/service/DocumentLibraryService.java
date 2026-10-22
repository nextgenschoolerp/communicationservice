package com.kvs.communication.service;

import com.kvs.core.exception.BadRequestException;
import com.kvs.core.exception.ConflictException;
import com.kvs.core.exception.ResourceNotFoundException;
import com.kvs.communication.dto.*;
import com.kvs.communication.entity.DocumentAccessLog;
import com.kvs.communication.entity.DocumentFolder;
import com.kvs.communication.entity.SchoolDocument;
import com.kvs.communication.entity.SchoolDocumentContent;
import com.kvs.communication.repo.DocumentAccessLogRepository;
import com.kvs.communication.repo.DocumentFolderRepository;
import com.kvs.communication.repo.SchoolDocumentContentRepository;
import com.kvs.communication.repo.SchoolDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** School document library (Documents page): folders, files, who can view them and their access history. */
@Service
@RequiredArgsConstructor
public class DocumentLibraryService {

    private static final Map<String, String> ACCESS_LABELS = Map.of(
            "ALL_STAFF", "All staff", "STAFF_AND_PARENTS", "Staff & parents",
            "PUBLIC", "Public", "ADMINISTRATORS", "School administrators");

    private final DocumentFolderRepository folderRepository;
    private final SchoolDocumentRepository documentRepository;
    private final SchoolDocumentContentRepository contentRepository;
    private final DocumentAccessLogRepository accessLogRepository;
    private final CommunicationAuditService auditService;

    @Value("${communication.time-zone:Asia/Kolkata}")
    private String timeZone;

    @Transactional(readOnly = true)
    public List<DocumentFolderResponse> listFolders(UUID tenantId) {
        Map<UUID, Long> counts = documentRepository.findByTenantIdOrderByModifiedOnDescFileNameAsc(tenantId).stream()
                .filter(d -> !Boolean.TRUE.equals(d.getIsArchived()))
                .collect(Collectors.groupingBy(SchoolDocument::getFolderId, Collectors.counting()));
        return folderRepository.findByTenantIdOrderBySortOrderAscNameAsc(tenantId).stream()
                .map(f -> mapFolder(f, counts.getOrDefault(f.getFolderId(), 0L)))
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentFolderResponse createFolder(DocumentFolderRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isEmpty()) {
            throw new BadRequestException("Folder name is required");
        }
        if (folderRepository.existsByTenantIdAndNameIgnoreCase(request.getTenantId(), name)) {
            throw new ConflictException("A folder named \"" + name + "\" already exists");
        }
        DocumentFolder folder = folderRepository.save(DocumentFolder.builder()
                .tenantId(request.getTenantId())
                .name(name)
                .defaultAccess(request.getDefaultAccess())
                .build());
        auditService.logSuccess("DocumentFolder", "CREATE_FOLDER", "Created document folder " + folder.getFolderId());
        return mapFolder(folder, 0);
    }

    @Transactional
    public DocumentFolderResponse updateFolder(UUID folderId, DocumentFolderRequest request) {
        DocumentFolder folder = folder(folderId);
        folder.setDefaultAccess(request.getDefaultAccess());
        auditService.logSuccess("DocumentFolder", "UPDATE_FOLDER", "Folder " + folderId + " default access " + request.getDefaultAccess());
        long count = documentRepository.findByTenantIdAndFolderIdOrderByModifiedOnDescFileNameAsc(folder.getTenantId(), folderId).stream()
                .filter(d -> !Boolean.TRUE.equals(d.getIsArchived())).count();
        return mapFolder(folder, count);
    }

    @Transactional(readOnly = true)
    public List<SchoolDocumentResponse> listDocuments(UUID tenantId, UUID folderId) {
        List<SchoolDocument> docs = folderId == null
                ? documentRepository.findByTenantIdOrderByModifiedOnDescFileNameAsc(tenantId)
                : documentRepository.findByTenantIdAndFolderIdOrderByModifiedOnDescFileNameAsc(tenantId, folderId);
        return docs.stream().map(this::mapDocument).collect(Collectors.toList());
    }

    @Transactional
    public SchoolDocumentResponse upload(UUID folderId, MultipartFile file, String ownerUserId, String ownerName) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Choose a file to upload");
        }
        DocumentFolder folder = folder(folderId);
        String fileName = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                ? "Untitled" : file.getOriginalFilename().replaceAll("[\\\\/]", "_");
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("The file could not be read");
        }
        SchoolDocument doc = documentRepository.save(SchoolDocument.builder()
                .tenantId(folder.getTenantId())
                .folderId(folderId)
                .fileName(fileName)
                .contentType(file.getContentType())
                .sizeBytes((long) bytes.length)
                .ownerUserId(ownerUserId)
                .ownerName(ownerName == null || ownerName.isBlank() ? "Staff member" : ownerName.trim())
                .accessLevel(folder.getDefaultAccess())
                .modifiedOn(LocalDate.now(ZoneId.of(timeZone)))
                .hasContent(true)
                .build());
        contentRepository.save(SchoolDocumentContent.builder().documentId(doc.getDocumentId()).bytes(bytes).build());
        log(doc, doc.getOwnerName(), "Uploaded version " + doc.getDocVersion());
        auditService.logSuccess("SchoolDocument", "UPLOAD_DOCUMENT", "Uploaded document " + doc.getDocumentId() + " to folder " + folderId);
        return mapDocument(doc);
    }

    @Transactional(readOnly = true)
    public SchoolDocument document(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("SchoolDocument", "documentId", documentId));
    }

    @Transactional(readOnly = true)
    public byte[] content(UUID documentId) {
        return contentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("No file is stored for document " + documentId))
                .getBytes();
    }

    @Transactional
    public SchoolDocumentResponse changeAccess(UUID documentId, DocumentAccessRequest request) {
        SchoolDocument doc = document(documentId);
        if (!request.getAccessLevel().equals(doc.getAccessLevel())) {
            doc.setAccessLevel(request.getAccessLevel());
            log(doc, request.getActorName(), "Access changed to " + ACCESS_LABELS.get(request.getAccessLevel()));
            auditService.logSuccess("SchoolDocument", "CHANGE_ACCESS", "Document " + documentId + " access " + request.getAccessLevel());
        }
        return mapDocument(doc);
    }

    @Transactional
    public DocumentActivityResponse recordActivity(UUID documentId, DocumentActivityRequest request) {
        SchoolDocument doc = document(documentId);
        if (Boolean.TRUE.equals(request.getShared())) {
            doc.setIsShared(true);
        }
        return mapLog(log(doc, request.getActorName(), request.getAction()));
    }

    @Transactional(readOnly = true)
    public List<DocumentActivityResponse> accessHistory(UUID documentId) {
        document(documentId);
        return accessLogRepository.findByDocumentIdOrderByOccurredAtDesc(documentId).stream()
                .map(this::mapLog).collect(Collectors.toList());
    }

    private DocumentFolder folder(UUID folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentFolder", "folderId", folderId));
    }

    private DocumentAccessLog log(SchoolDocument doc, String actor, String action) {
        return accessLogRepository.save(DocumentAccessLog.builder()
                .tenantId(doc.getTenantId())
                .documentId(doc.getDocumentId())
                .actorName(actor)
                .action(action)
                .occurredAt(Instant.now())
                .build());
    }

    private DocumentFolderResponse mapFolder(DocumentFolder f, long count) {
        return DocumentFolderResponse.builder()
                .folderId(f.getFolderId())
                .name(f.getName())
                .defaultAccess(f.getDefaultAccess())
                .summary(f.getSummary())
                .documentCount(count)
                .build();
    }

    private SchoolDocumentResponse mapDocument(SchoolDocument d) {
        return SchoolDocumentResponse.builder()
                .documentId(d.getDocumentId())
                .folderId(d.getFolderId())
                .fileName(d.getFileName())
                .contentType(d.getContentType())
                .sizeBytes(d.getSizeBytes())
                .ownerName(d.getOwnerName())
                .accessLevel(d.getAccessLevel())
                .docVersion(d.getDocVersion())
                .modifiedOn(d.getModifiedOn())
                .isShared(d.getIsShared())
                .isArchived(d.getIsArchived())
                .hasContent(d.getHasContent())
                .build();
    }

    private DocumentActivityResponse mapLog(DocumentAccessLog l) {
        return DocumentActivityResponse.builder()
                .logId(l.getLogId())
                .actorName(l.getActorName())
                .action(l.getAction())
                .occurredAt(l.getOccurredAt())
                .build();
    }
}
