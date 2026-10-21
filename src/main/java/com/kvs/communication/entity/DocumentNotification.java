package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "document_notifications", indexes = {
        @Index(name = "idx_doc_notif_student", columnList = "student_id"),
        @Index(name = "idx_doc_notif_type", columnList = "document_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentNotification extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id", updatable = false, nullable = false)
    private UUID notificationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType; // FEE_RECEIPT, REPORT_CARD

    @Column(name = "document_id", nullable = false, length = 100)
    private String documentId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "parent_phone", nullable = false, length = 20)
    private String parentPhone;

    @Column(name = "parent_email", length = 100)
    private String parentEmail;

    @Column(name = "download_url", nullable = false, length = 500)
    private String downloadUrl;

    @Column(name = "delivery_status", length = 20)
    @Builder.Default
    private String deliveryStatus = "DELIVERED";
}
