package com.jantabank.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request to set/change a card PIN. The PIN is stored only as a salted hash.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetPinRequest {

    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^[0-9]{4}$", message = "PIN must be exactly 4 digits")
    private String pin;
}
