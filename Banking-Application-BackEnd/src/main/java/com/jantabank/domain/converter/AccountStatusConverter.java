package com.jantabank.domain.converter;

import com.jantabank.domain.enums.AccountStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link AccountStatus} as its stable integer code (BIGINT column).
 */
@Converter(autoApply = true)
public class AccountStatusConverter implements AttributeConverter<AccountStatus, Long> {

    @Override
    public Long convertToDatabaseColumn(AccountStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public AccountStatus convertToEntityAttribute(Long dbData) {
        return AccountStatus.fromCode(dbData);
    }
}
