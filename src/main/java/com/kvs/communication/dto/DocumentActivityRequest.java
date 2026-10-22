package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/** Something done to a document in the browser (download, share) that belongs in its access history. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentActivityRequest {

    @NotBlank(message = "Actor name is required")
    @Size(max = 150)
    private String actorName;

    @NotBlank(message = "Action is required")
    @Size(max = 200)
    private String action;

    /** True when the action shared the document, so it shows under "Shared with me" */
    private Boolean shared;
}
