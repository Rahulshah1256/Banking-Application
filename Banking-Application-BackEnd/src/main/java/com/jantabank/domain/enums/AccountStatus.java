package com.jantabank.domain.enums;

/**
 * Lifecycle status of a bank account. Codes preserve the legacy integer values
 * previously stored in the {@code accounts.status} column.
 */
public enum AccountStatus implements CodedEnum {
    REQUESTED(0),
    ACTIVE(1),
    INACTIVE(2),
    CLOSED(3);

    private final long code;

    AccountStatus(long code) {
        this.code = code;
    }

    @Override
    public long getCode() {
        return code;
    }

    public static AccountStatus fromCode(Long code) {
        if (code == null) {
            return null;
        }
        for (AccountStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown AccountStatus code: " + code);
    }
}
