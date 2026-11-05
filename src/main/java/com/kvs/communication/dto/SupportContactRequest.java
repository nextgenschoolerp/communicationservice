package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/** Save the Contact support card for a school. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportContactRequest {

    @NotBlank
    private String tenantId;
    @Size(max = 200)
    private String email;
    @Size(max = 50)
    private String phone;
    /** DayOfWeek names, e.g. MONDAY */
    @NotEmpty
    private List<String> openDays;
    /** HH:mm */
    @NotBlank
    private String opensAt;
    /** HH:mm */
    @NotBlank
    private String closesAt;
    @NotBlank
    @Size(max = 100)
    private String responseTime;
}
