package com.kvs.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyBulletinRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Bulletin type is required (e.g., CAFETERIA_MENU, BUS_SCHEDULE)")
    @Size(max = 30)
    private String bulletinType;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Content details are required")
    private String contentDetails;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @Size(max = 30)
    private String mealType;

    @Size(max = 50)
    private String busRouteNumber;
}
