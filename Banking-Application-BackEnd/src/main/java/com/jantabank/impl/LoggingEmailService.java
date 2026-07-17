package com.jantabank.impl;

import com.jantabank.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Development email implementation that logs the password-reset link instead of
 * dispatching a real email. Swap for an SMTP/provider-backed implementation in
 * production by providing another {@link EmailService} bean.
 */
@Service
public class LoggingEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    private final String resetBaseUrl;
    private final String verificationBaseUrl;

    public LoggingEmailService(@Value("${app.password-reset-url:http://localhost:3000/reset-password}") String resetBaseUrl,
                               @Value("${app.email-verification-url:http://localhost:3000/verify-email}") String verificationBaseUrl) {
        this.resetBaseUrl = resetBaseUrl;
        this.verificationBaseUrl = verificationBaseUrl;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String recipientName, String resetToken) {
        String link = resetBaseUrl + "?token=" + resetToken;
        log.info("[DEV-EMAIL] Password reset requested for {} ({}). Reset link: {}",
                recipientName, toEmail, link);
    }

    @Override
    public void sendEmailVerification(String toEmail, String recipientName, String verificationToken) {
        String link = verificationBaseUrl + "?token=" + verificationToken;
        log.info("[DEV-EMAIL] Email verification for {} ({}). Verify link: {}",
                recipientName, toEmail, link);
    }

    @Override
    public void sendLoginOtp(String toEmail, String recipientName, String otp) {
        log.info("[DEV-EMAIL] Login OTP for {} ({}): {}", recipientName, toEmail, otp);
    }
}
