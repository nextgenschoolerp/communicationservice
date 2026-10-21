package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorCheckInRequest {

    @NotBlank(message = "Badge number is required")
    private String badgeNumber;
}
