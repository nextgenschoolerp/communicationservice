package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/** One message in a support ticket's conversation; the first one is the request itself. */
@Entity
@Table(name = "support_ticket_messages", indexes = {
        @Index(name = "idx_support_ticket_messages_ticket", columnList = "ticket_id, sent_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketMessage extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "message_id", updatable = false, nullable = false)
    private UUID messageId;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "author_name", nullable = false, length = 150)
    private String authorName;

    /** REQUESTER (school staff) or SUPPORT (workspace support team) */
    @Column(name = "author_role", nullable = false, length = 20)
    private String authorRole;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;
}
