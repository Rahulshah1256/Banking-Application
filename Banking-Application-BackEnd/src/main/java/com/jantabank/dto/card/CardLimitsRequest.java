package com.jantabank.dto.card;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request to update per-channel daily spend limits. Null fields are unchanged.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardLimitsRequest {

    @PositiveOrZero(message = "ATM limit cannot be negative")
    private Double atmDailyLimit;

    @PositiveOrZero(message = "POS limit cannot be negative")
    private Double posDailyLimit;

    @PositiveOrZero(message = "Online limit cannot be negative")
    private Double onlineDailyLimit;
}
