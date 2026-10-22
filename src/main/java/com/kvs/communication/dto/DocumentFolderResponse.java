package com.kvs.communication.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFolderResponse {

    private UUID folderId;
    private String name;
    private String defaultAccess;
    private String summary;
    private long documentCount;
}
