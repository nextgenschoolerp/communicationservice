package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSlotRequest {

    @NotNull(message = "Parent user ID is required")
    private UUID parentId;

    @NotBlank(message = "Parent name is required")
    @Size(max = 150)
    private String parentName;
}
