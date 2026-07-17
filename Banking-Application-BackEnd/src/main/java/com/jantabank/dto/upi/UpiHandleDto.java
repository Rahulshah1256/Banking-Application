package com.jantabank.dto.upi;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Read model for a registered UPI VPA. The underlying account number is masked.
 */
@Getter
@Setter
@Builder
public class UpiHandleDto {

    private Long id;
    private String vpa;
    private String maskedAccount;
    private boolean primary;
    private boolean active;
}
