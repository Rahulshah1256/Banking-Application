package com.jantabank.dto.card;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request to block a card, with an optional reason.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockCardRequest {

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;
}
