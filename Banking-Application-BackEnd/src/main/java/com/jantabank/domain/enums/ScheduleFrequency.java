package com.jantabank.domain.enums;

/**
 * Cadence for a scheduled transfer. ONCE fires a single time on the scheduled
 * date; the others repeat until cancelled.
 */
public enum ScheduleFrequency {
    ONCE,
    DAILY,
    WEEKLY,
    MONTHLY
}
