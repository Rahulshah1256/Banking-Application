package com.jantabank.domain.enums;

/**
 * Activation status of a beneficiary. Codes preserve the legacy integer values
 * previously stored in the {@code beneficiaries.status} column
 * ({@code 1} == active).
 */
public enum BeneficiaryStatus implements CodedEnum {
    INACTIVE(0),
    ACTIVE(1),
    PENDING(2);

    private final long code;

    BeneficiaryStatus(long code) {
        this.code = code;
    }

    @Override
    public long getCode() {
        return code;
    }

    public static BeneficiaryStatus fromCode(Long code) {
        if (code == null) {
            return null;
        }
        for (BeneficiaryStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown BeneficiaryStatus code: " + code);
    }
}
