package com.kvs.communication.entity;

import com.kvs.core.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "parent_student_mappings", indexes = {
        @Index(name = "idx_parent_mapping_parent", columnList = "parent_user_id"),
        @Index(name = "idx_parent_mapping_student", columnList = "student_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentStudentMapping extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "mapping_id", updatable = false, nullable = false)
    private UUID mappingId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "parent_user_id", nullable = false)
    private UUID parentUserId;

    @Column(name = "parent_phone", nullable = false, length = 20)
    private String parentPhone;

    @Column(name = "parent_name", nullable = false, length = 150)
    private String parentName;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "student_name", nullable = false, length = 150)
    private String studentName;

    @Column(name = "grade", nullable = false, length = 20)
    private String grade;

    @Column(name = "section", nullable = false, length = 10)
    private String section;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = true;

    @Column(name = "relationship", length = 30)
    @Builder.Default
    private String relationship = "FATHER";
}
