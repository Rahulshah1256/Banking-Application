package com.jantabank.dto.deposit;

import lombok.Getter;
import lombok.Setter;

/**
 * Toggle auto-renewal on a deposit.
 */
@Getter
@Setter
public class AutoRenewRequest {
    private boolean autoRenew;
}
