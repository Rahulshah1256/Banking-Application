package com.jantabank.domain.enums;

/**
 * Lifecycle status of a customer/user. Codes preserve the legacy integer values
 * previously stored in the {@code users.status} column.
 */
public enum UserStatus implements CodedEnum {
    REQUESTED(0),
    ACTIVE(1),
    INACTIVE(2),
    REJECTED(4);

    private final long code;

    UserStatus(long code) {
        this.code = code;
    }

    @Override
    public long getCode() {
        return code;
    }

    public static UserStatus fromCode(Long code) {
        if (code == null) {
            return null;
        }
        for (UserStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown UserStatus code: " + code);
    }
}
