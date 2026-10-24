package com.kvs.communication.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildProfileResponse {

    private UUID mappingId;
    private String tenantId;
    private String parentUserId;
    private String parentName;
    private UUID studentId;
    private String studentName;
    private String grade;
    private String section;
    private Boolean isPrimary;
    private String relationship;
}
