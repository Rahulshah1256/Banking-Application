package com.jantabank.domain.converter;

import com.jantabank.domain.enums.BeneficiaryStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link BeneficiaryStatus} as its stable integer code (BIGINT column).
 */
@Converter(autoApply = true)
public class BeneficiaryStatusConverter implements AttributeConverter<BeneficiaryStatus, Long> {

    @Override
    public Long convertToDatabaseColumn(BeneficiaryStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public BeneficiaryStatus convertToEntityAttribute(Long dbData) {
        return BeneficiaryStatus.fromCode(dbData);
    }
}
