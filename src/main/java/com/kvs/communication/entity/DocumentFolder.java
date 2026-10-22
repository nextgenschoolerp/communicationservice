package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "document_folders", uniqueConstraints = {
        @UniqueConstraint(name = "uk_document_folder_tenant_name", columnNames = {"tenant_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentFolder extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "folder_id", updatable = false, nullable = false)
    private UUID folderId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Who can view files uploaded to this folder: ALL_STAFF, STAFF_AND_PARENTS, PUBLIC or ADMINISTRATORS */
    @Column(name = "default_access", nullable = false, length = 30)
    @Builder.Default
    private String defaultAccess = "ALL_STAFF";

    /** Display line for folders summarised elsewhere (e.g. "1,248 folders"); null shows the document count */
    @Column(name = "summary", length = 100)
    private String summary;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 100;
}
