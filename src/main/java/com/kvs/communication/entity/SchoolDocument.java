package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "school_documents", indexes = {
        @Index(name = "idx_school_documents_folder", columnList = "tenant_id, folder_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolDocument extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id", updatable = false, nullable = false)
    private UUID documentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "folder_id", nullable = false)
    private UUID folderId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    /** authservice user IDs are text (not always UUIDs) */
    @Column(name = "owner_user_id", length = 100)
    private String ownerUserId;

    @Column(name = "owner_name", nullable = false, length = 150)
    private String ownerName;

    @Column(name = "access_level", nullable = false, length = 30)
    private String accessLevel;

    @Column(name = "doc_version", nullable = false, length = 20)
    @Builder.Default
    private String docVersion = "1.0";

    @Column(name = "modified_on", nullable = false)
    private LocalDate modifiedOn;

    @Column(name = "is_shared")
    @Builder.Default
    private Boolean isShared = false;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean isArchived = false;

    /** False for reference rows seeded without a file; the UI then renders a placeholder */
    @Column(name = "has_content")
    @Builder.Default
    private Boolean hasContent = false;
}
