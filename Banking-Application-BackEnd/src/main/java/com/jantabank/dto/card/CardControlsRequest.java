package com.jantabank.dto.card;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request to toggle card usage channels. Null fields are left unchanged.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardControlsRequest {

    private Boolean internationalEnabled;
    private Boolean onlineEnabled;
    private Boolean contactlessEnabled;
}
