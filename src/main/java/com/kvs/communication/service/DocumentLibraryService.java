package com.kvs.communication.service;

import com.kvs.core.exception.BadRequestException;
import com.kvs.core.exception.ConflictException;
import com.kvs.core.exception.ResourceNotFoundException;
import com.kvs.communication.dto.*;
import com.kvs.communication.entity.DocumentAccessLog;
import com.kvs.communication.entity.DocumentFolder;
import com.kvs.communication.entity.SchoolDocument;
import com.kvs.communication.repo.DocumentAccessLogRepository;
import com.kvs.communication.repo.DocumentFolderRepository;
import com.kvs.communication.repo.SchoolDocumentRepository;
import com.kvs.core.storage.FileResponses;
import com.kvs.core.storage.FileUploads;
import com.kvs.core.storage.StoredFile;
import com.kvs.core.storage.UploadPolicy;
import com.kvs.core.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * School document library (Documents page): folders, files, who can view them and their access history.
 * Files are kept in the shared file store (ms-core {@link FileUploads}, area {@code library}).
 */
@Service
@RequiredArgsConstructor
public class DocumentLibraryService {

    private static final Map<String, String> ACCESS_LABELS = Map.of(
            "ALL_STAFF", "All staff", "STAFF_AND_PARENTS", "Staff & parents",
            "PUBLIC", "Public", "ADMINISTRATORS", "School administrators");

    /** Roles that may open an ADMINISTRATORS document: the school's leadership. */
    private static final Set<String> ADMINISTRATOR_ROLES = Set.of(
            "SUPER_ADMIN", "ADMIN", "TRUSTEE", "PRINCIPAL", "SCHOOL_PRINCIPAL", "VICE_PRINCIPAL");

    /** Parent roles: they see only the documents shared with parents. */
    private static final Set<String> PARENT_ROLES = Set.of("PARENT", "APPLICANT_PARENT");

    private final DocumentFolderRepository folderRepository;
    private final SchoolDocumentRepository documentRepository;
    private final DocumentAccessLogRepository accessLogRepository;
    private final FileUploads fileUploads;
    private final CommunicationAuditService auditService;

    @Value("${communication.time-zone:Asia/Kolkata}")
    private String timeZone;

    @Transactional(readOnly = true)
    public List<DocumentFolderResponse> listFolders(String tenantId) {
        Collection<String> roles = SecurityUtils.getCurrentUserRoles();
        Map<UUID, Long> counts = documentRepository.findByTenantIdOrderByModifiedOnDescFileNameAsc(tenantId).stream()
                .filter(d -> !Boolean.TRUE.equals(d.getIsArchived()))
                .filter(d -> canView(d.getAccessLevel(), roles))
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
                .filter(d -> !Boolean.TRUE.equals(d.getIsArchived()))
                .filter(d -> canView(d.getAccessLevel(), SecurityUtils.getCurrentUserRoles()))
                .count();
        return mapFolder(folder, count);
    }

    @Transactional(readOnly = true)
    public List<SchoolDocumentResponse> listDocuments(String tenantId, UUID folderId) {
        Collection<String> roles = SecurityUtils.getCurrentUserRoles();
        List<SchoolDocument> docs = folderId == null
                ? documentRepository.findByTenantIdOrderByModifiedOnDescFileNameAsc(tenantId)
                : documentRepository.findByTenantIdAndFolderIdOrderByModifiedOnDescFileNameAsc(tenantId, folderId);
        return docs.stream()
                .filter(d -> canView(d.getAccessLevel(), roles))
                .map(this::mapDocument)
                .collect(Collectors.toList());
    }

    @Transactional
    public SchoolDocumentResponse upload(UUID folderId, MultipartFile file, String ownerUserId, String ownerName) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Choose a file to upload");
        }
        DocumentFolder folder = folder(folderId);
        // the file is checked (type, size, signature) and saved; it is removed again if this transaction rolls back
        StoredFile stored = fileUploads.save(folder.getTenantId(), UploadPolicy.LIBRARY_DOCUMENT, folderId, file);
        SchoolDocument doc = documentRepository.save(SchoolDocument.builder()
                .tenantId(folder.getTenantId())
                .folderId(folderId)
                .fileName(stored.fileName())
                .contentType(stored.contentType())
                .sizeBytes(stored.sizeBytes())
                .checksumSha256(stored.sha256())
                .storageKey(stored.key())
                .ownerUserId(ownerUserId)
                .ownerName(ownerName == null || ownerName.isBlank() ? "Staff member" : ownerName.trim())
                .accessLevel(folder.getDefaultAccess())
                .modifiedOn(LocalDate.now(ZoneId.of(timeZone)))
                .hasContent(true)
                .build());
        log(doc, doc.getOwnerName(), "Uploaded version " + doc.getDocVersion());
        auditService.logSuccess("SchoolDocument", "UPLOAD_DOCUMENT", "Uploaded document " + doc.getDocumentId() + " to folder " + folderId);
        return mapDocument(doc);
    }

    @Transactional(readOnly = true)
    public SchoolDocument document(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("SchoolDocument", "documentId", documentId));
    }

    /**
     * The file for download. A document the caller may not see answers 404, as it does in the list.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> documentFile(UUID documentId) {
        SchoolDocument doc = visibleDocument(documentId);
        if (doc.getStorageKey() == null) {
            throw new ResourceNotFoundException("No file is stored for document " + documentId);
        }
        return FileResponses.attachment(fileUploads.store(), doc.getStorageKey(), doc.getContentType(), doc.getFileName());
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
        SchoolDocument doc = visibleDocument(documentId);
        if (Boolean.TRUE.equals(request.getShared())) {
            doc.setIsShared(true);
        }
        return mapLog(log(doc, request.getActorName(), request.getAction()));
    }

    @Transactional(readOnly = true)
    public List<DocumentActivityResponse> accessHistory(UUID documentId) {
        visibleDocument(documentId);
        return accessLogRepository.findByDocumentIdOrderByOccurredAtDesc(documentId).stream()
                .map(this::mapLog).collect(Collectors.toList());
    }

    /**
     * Whether a caller with these authorities may see a document of this access level.
     * ALL_STAFF: anyone except a parent. STAFF_AND_PARENTS and PUBLIC: any signed-in user.
     * ADMINISTRATORS: the school's leadership only.
     */
    static boolean canView(String accessLevel, Collection<String> authorities) {
        if (accessLevel == null) {
            return false;
        }
        return switch (accessLevel) {
            case "PUBLIC", "STAFF_AND_PARENTS" -> true;
            case "ALL_STAFF" -> PARENT_ROLES.stream().noneMatch(r -> authorities.contains("ROLE_" + r));
            case "ADMINISTRATORS" -> ADMINISTRATOR_ROLES.stream().anyMatch(r -> authorities.contains("ROLE_" + r));
            default -> false;
        };
    }

    /** The document, or 404 when the caller may not see it (so a hidden file is not revealed). */
    private SchoolDocument visibleDocument(UUID documentId) {
        SchoolDocument doc = document(documentId);
        if (!canView(doc.getAccessLevel(), SecurityUtils.getCurrentUserRoles())) {
            throw new ResourceNotFoundException("SchoolDocument", "documentId", documentId);
        }
        return doc;
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
