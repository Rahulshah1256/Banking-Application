package com.jantabank.domain.converter;

import com.jantabank.domain.enums.TransactionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link TransactionStatus} as its stable integer code (BIGINT column).
 */
@Converter(autoApply = true)
public class TransactionStatusConverter implements AttributeConverter<TransactionStatus, Long> {

    @Override
    public Long convertToDatabaseColumn(TransactionStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public TransactionStatus convertToEntityAttribute(Long dbData) {
        return TransactionStatus.fromCode(dbData);
    }
}
