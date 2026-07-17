package com.jantabank.dto.profile;

import com.jantabank.domain.enums.KycStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin decision on a KYC document. Only VERIFIED or REJECTED are accepted.
 */
@Getter
@Setter
public class VerifyKycRequest {

    @NotNull(message = "Decision status is required")
    private KycStatus status;

    @Size(max = 255)
    private String remarks;
}
