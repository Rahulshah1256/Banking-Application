package com.jantabank.dto.upi;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Result of resolving a payee VPA. Exposes only non-sensitive payee details.
 */
@Getter
@Setter
@Builder
public class UpiResolveDto {

    private String vpa;
    private String payeeName;
    private String maskedAccount;
    private boolean active;
}
