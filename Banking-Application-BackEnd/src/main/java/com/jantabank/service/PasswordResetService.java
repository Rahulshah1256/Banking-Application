package com.jantabank.service;

import com.jantabank.dto.ChangePasswordRequest;
import com.jantabank.dto.ForgotPasswordRequest;
import com.jantabank.dto.ResetPasswordRequest;

public interface PasswordResetService {

    /** Changes the password for the authenticated user after verifying the old one. */
    void changePassword(String username, ChangePasswordRequest request);

    /**
     * Initiates a password reset for the account matching the email (if any) and
     * dispatches a reset link. Always succeeds silently to avoid user enumeration.
     */
    void forgotPassword(ForgotPasswordRequest request);

    /** Completes a password reset using a valid, unused, unexpired token. */
    void resetPassword(ResetPasswordRequest request);
}
