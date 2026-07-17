package com.jantabank.domain.enums;

/**
 * Lifecycle of a scheduled/recurring transfer instruction.
 */
public enum ScheduleStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    FAILED
}
