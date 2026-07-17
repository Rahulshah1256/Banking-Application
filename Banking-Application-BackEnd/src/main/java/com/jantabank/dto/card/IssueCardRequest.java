package com.jantabank.dto.card;

import com.jantabank.domain.enums.CardNetwork;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request to issue a new debit card against one of the caller's accounts.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IssueCardRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Card network is required")
    private CardNetwork network;
}
