package com.jantabank.service;

import com.jantabank.dto.JwtAuthResponse;
import com.jantabank.dto.OtpChallengeResponse;
import com.jantabank.dto.OtpRequestRequest;
import com.jantabank.dto.OtpVerifyRequest;
import com.jantabank.web.ClientMetadata;

public interface OtpService {

    /** Validates credentials, issues a one-time login OTP and dispatches it. */
    OtpChallengeResponse requestOtp(OtpRequestRequest request, ClientMetadata metadata);

    /** Verifies a login OTP and, on success, issues JWT access + refresh tokens. */
    JwtAuthResponse verifyOtp(OtpVerifyRequest request, ClientMetadata metadata);
}
