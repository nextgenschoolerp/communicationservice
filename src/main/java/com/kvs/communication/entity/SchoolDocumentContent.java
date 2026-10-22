package com.kvs.communication.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/** File bytes, kept apart from school_documents so listing a folder never loads them. */
@Entity
@Table(name = "school_document_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolDocumentContent {

    @Id
    @Column(name = "document_id", updatable = false, nullable = false)
    private UUID documentId;

    @Column(name = "bytes", nullable = false, columnDefinition = "bytea")
    private byte[] bytes;
}
