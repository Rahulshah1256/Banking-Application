package com.jantabank.domain.enums;

/**
 * Status of a money-movement transaction. Codes preserve the legacy integer
 * values previously stored in the {@code transactions.status} column.
 */
public enum TransactionStatus implements CodedEnum {
    INITIATED(0),
    COMPLETED(1),
    FAILED(2);

    private final long code;

    TransactionStatus(long code) {
        this.code = code;
    }

    @Override
    public long getCode() {
        return code;
    }

    public static TransactionStatus fromCode(Long code) {
        if (code == null) {
            return null;
        }
        for (TransactionStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown TransactionStatus code: " + code);
    }
}
