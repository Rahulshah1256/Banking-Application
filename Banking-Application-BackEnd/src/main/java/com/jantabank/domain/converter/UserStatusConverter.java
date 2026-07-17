package com.jantabank.domain.converter;

import com.jantabank.domain.enums.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link UserStatus} as its stable integer code (BIGINT column),
 * keeping backwards compatibility with existing {@code users.status} data.
 */
@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, Long> {

    @Override
    public Long convertToDatabaseColumn(UserStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public UserStatus convertToEntityAttribute(Long dbData) {
        return UserStatus.fromCode(dbData);
    }
}
