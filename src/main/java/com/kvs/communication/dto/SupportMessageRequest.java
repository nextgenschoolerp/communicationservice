package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportMessageRequest {

    @NotBlank(message = "Author name is required")
    @Size(max = 150)
    private String authorName;

    /** REQUESTER by default; SUPPORT for the support team's replies */
    @Pattern(regexp = "REQUESTER|SUPPORT", message = "Author role must be REQUESTER or SUPPORT")
    private String authorRole;

    @NotBlank(message = "Write a message")
    @Size(max = 4000)
    private String body;
}
