package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/** New folder (tenantId and name required) or folder settings (only defaultAccess is applied). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFolderRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @Size(max = 150, message = "Folder name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Default access is required")
    @Pattern(regexp = DocumentAccessRequest.ACCESS_LEVELS, message = "Access must be ALL_STAFF, STAFF_AND_PARENTS, PUBLIC or ADMINISTRATORS")
    private String defaultAccess;
}
