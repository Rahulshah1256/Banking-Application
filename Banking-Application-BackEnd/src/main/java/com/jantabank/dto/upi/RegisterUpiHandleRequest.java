package com.jantabank.dto.upi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request to register a UPI VPA against one of the caller's accounts.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUpiHandleRequest {

    @NotBlank(message = "VPA is required")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]{2,50}@[a-zA-Z0-9]{2,30}$",
            message = "VPA must look like name@handle")
    private String vpa;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    private boolean primary;
}
