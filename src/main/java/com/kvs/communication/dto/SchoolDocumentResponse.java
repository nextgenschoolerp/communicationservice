package com.kvs.communication.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDocumentResponse {

    private UUID documentId;
    private UUID folderId;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    private String ownerName;
    private String accessLevel;
    private String docVersion;
    private LocalDate modifiedOn;
    private Boolean isShared;
    private Boolean isArchived;
    private Boolean hasContent;
}
