package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "calendar_events", indexes = {
        @Index(name = "idx_calendar_tenant_dates", columnList = "tenant_id, start_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", updatable = false, nullable = false)
    private UUID eventId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // EXAM, HOLIDAY, SPORTS, CULTURAL, MEETING

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** "HH:mm"; null for an all-day event */
    @Column(name = "start_time", length = 5)
    private String startTime;

    @Column(name = "end_time", length = 5)
    private String endTime;

    @Column(name = "venue", length = 150)
    private String venue;

    @Column(name = "organiser", length = 150)
    private String organiser;

    @Column(name = "target_audience", length = 50)
    @Builder.Default
    private String targetAudience = "ALL";

    @Column(name = "media_urls", columnDefinition = "TEXT")
    private String mediaUrls;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = true;
}
