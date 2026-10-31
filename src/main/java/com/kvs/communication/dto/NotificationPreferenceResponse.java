package com.kvs.communication.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {

    private UUID preferenceId;
    private String tenantId;
    private String userId;
    private Boolean whatsappEnabled;
    private Boolean smsEnabled;
    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private String quietHoursStart;
    private String quietHoursEnd;
}
