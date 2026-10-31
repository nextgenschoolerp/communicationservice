package com.kvs.communication.enums;

public enum DeliveryStatus {
    /** saved with the circular, not sent yet */
    QUEUED,
    SENT,
    DELIVERED,
    READ,
    FAILED,
    /** not sent: no mobile number or email, no provider set up, or app push */
    SKIPPED
}
