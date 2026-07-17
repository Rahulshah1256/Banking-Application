package com.jantabank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequestRequest {

    @NotBlank(message = "Username or email is required")
    private String usernameoremail;

    @NotBlank(message = "Password is required")
    private String password;
}
