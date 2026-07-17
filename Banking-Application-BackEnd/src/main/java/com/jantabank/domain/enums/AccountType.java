package com.jantabank.domain.enums;

/**
 * Type of a bank account. The two-digit {@link #getCode()} is used when
 * generating account numbers; the {@link #name()} is persisted as a label.
 */
public enum AccountType implements CodedEnum {
    SAVINGS(1),
    CURRENT(2);

    private final long code;

    AccountType(long code) {
        this.code = code;
    }

    @Override
    public long getCode() {
        return code;
    }

    public static AccountType fromCode(Long code) {
        if (code == null) {
            return null;
        }
        for (AccountType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown AccountType code: " + code);
    }
}
