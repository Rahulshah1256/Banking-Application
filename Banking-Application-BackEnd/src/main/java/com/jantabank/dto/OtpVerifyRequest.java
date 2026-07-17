package com.jantabank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyRequest {

    @NotBlank(message = "Username or email is required")
    private String usernameoremail;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^[0-9]{4,9}$", message = "OTP must be numeric")
    private String otp;
}
