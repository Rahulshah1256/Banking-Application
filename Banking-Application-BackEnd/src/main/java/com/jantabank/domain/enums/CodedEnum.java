package com.jantabank.domain.enums;

/**
 * Contract for enums that are persisted using a stable integer code
 * rather than their ordinal, so reordering enum constants never corrupts data.
 */
public interface CodedEnum {
    long getCode();
}
