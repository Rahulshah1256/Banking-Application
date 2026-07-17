package com.jantabank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response to a successful OTP-login challenge request, telling the client that
 * an OTP has been dispatched and how long it remains valid.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpChallengeResponse {
    private String message;
    private long expiresInSeconds;
}
