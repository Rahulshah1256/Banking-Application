package com.jantabank.service;

/**
 * Abstraction over outbound transactional email. The default development
 * implementation logs messages; a production implementation can be backed by
 * {@code JavaMailSender} / an email provider without touching callers.
 */
public interface EmailService {

    /**
     * Sends a password-reset message containing the reset link/token to the
     * given recipient.
     */
    void sendPasswordResetEmail(String toEmail, String recipientName, String resetToken);

    /**
     * Sends an email-verification message containing the verification link/token
     * to the given recipient.
     */
    void sendEmailVerification(String toEmail, String recipientName, String verificationToken);

    /** Sends a one-time login OTP to the given recipient. */
    void sendLoginOtp(String toEmail, String recipientName, String otp);
}
