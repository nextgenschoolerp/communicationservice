package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAccessRequest {

    public static final String ACCESS_LEVELS = "ALL_STAFF|STAFF_AND_PARENTS|PUBLIC|ADMINISTRATORS";

    @NotBlank(message = "Access level is required")
    @Pattern(regexp = ACCESS_LEVELS, message = "Access must be ALL_STAFF, STAFF_AND_PARENTS, PUBLIC or ADMINISTRATORS")
    private String accessLevel;

    @NotBlank(message = "Actor name is required")
    @Size(max = 150)
    private String actorName;
}
