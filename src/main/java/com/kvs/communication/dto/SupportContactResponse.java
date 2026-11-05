package com.kvs.communication.dto;

import lombok.*;

import java.util.List;

/** Help & support -> Contact support card: how to reach the support team and whether it is open right now. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportContactResponse {

    private String email;
    private String phone;
    /** e.g. "Mon–Sat · 08:00–17:00 IST" */
    private String hours;
    private String responseTime;
    private List<String> openDays;
    private String opensAt;
    private String closesAt;
    private String timeZone;
    /** True when the support desk is open at this moment (open day, within opening hours, in the school's time zone). */
    private boolean online;
}
