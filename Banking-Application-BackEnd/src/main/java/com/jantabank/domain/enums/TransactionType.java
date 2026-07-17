package com.jantabank.domain.enums;

/**
 * High-level classification of a money-movement record.
 */
public enum TransactionType {
    TRANSFER,
    SCHEDULED_TRANSFER,
    RECURRING_TRANSFER,
    UPI
}
