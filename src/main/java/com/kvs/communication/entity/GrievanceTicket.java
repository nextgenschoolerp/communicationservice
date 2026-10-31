package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import com.kvs.communication.enums.TicketPriority;
import com.kvs.communication.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "grievance_tickets", indexes = {
        @Index(name = "idx_grievance_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_grievance_parent", columnList = "parent_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceTicket extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ticket_id", updatable = false, nullable = false)
    private UUID ticketId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "parent_user_id", nullable = false)
    private String parentUserId;

    @Column(name = "parent_name", nullable = false, length = 150)
    private String parentName;

    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "student_name", length = 150)
    private String studentName;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // ACADEMIC, TRANSPORT, FEES, DISCIPLINE, FACILITY

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    @Builder.Default
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    @Column(name = "sla_due_hours")
    @Builder.Default
    private Integer slaDueHours = 48;

    @Column(name = "assigned_to_user_id")
    private String assignedToUserId;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
