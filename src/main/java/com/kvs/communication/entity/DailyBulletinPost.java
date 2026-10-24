package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_bulletin_posts", indexes = {
        @Index(name = "idx_bulletin_tenant_date", columnList = "tenant_id, effective_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyBulletinPost extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bulletin_id", updatable = false, nullable = false)
    private UUID bulletinId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "bulletin_type", nullable = false, length = 30)
    private String bulletinType; // CAFETERIA_MENU, BUS_SCHEDULE

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content_details", nullable = false, columnDefinition = "TEXT")
    private String contentDetails;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "meal_type", length = 30)
    private String mealType; // BREAKFAST, LUNCH, SNACK

    @Column(name = "bus_route_number", length = 50)
    private String busRouteNumber;
}
