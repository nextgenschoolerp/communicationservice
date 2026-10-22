package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/** A request from school staff to the workspace support team (Help & support page). */
@Entity
@Table(name = "support_tickets", indexes = {
        @Index(name = "idx_support_tickets_requester", columnList = "tenant_id, requester_user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_support_ticket_number", columnNames = {"ticket_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ticket_id", updatable = false, nullable = false)
    private UUID ticketId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    /** e.g. SUP-1049 */
    @Column(name = "ticket_number", nullable = false, length = 20)
    private String ticketNumber;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    /** ACCOUNT_ACCESS, STUDENTS, ATTENDANCE, FEES, ACADEMICS, OTHER */
    @Column(name = "category", nullable = false, length = 30)
    private String category;

    /** NORMAL or URGENT */
    @Column(name = "priority", nullable = false, length = 10)
    @Builder.Default
    private String priority = "NORMAL";

    /** OPEN, IN_PROGRESS or RESOLVED */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN";

    /** authservice user IDs are text */
    @Column(name = "requester_user_id", nullable = false, length = 100)
    private String requesterUserId;

    @Column(name = "requester_name", nullable = false, length = 150)
    private String requesterName;

    @Column(name = "requester_email", length = 150)
    private String requesterEmail;

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
