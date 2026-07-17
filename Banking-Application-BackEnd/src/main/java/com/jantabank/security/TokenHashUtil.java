package com.jantabank.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Helpers for generating opaque security tokens and computing their SHA-256
 * hashes. Only hashes are ever persisted, so a database leak does not expose
 * usable tokens.
 */
public final class TokenHashUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TokenHashUtil() {
    }

    /** Generates a URL-safe, unpredictable opaque token of the given byte length. */
    public static String generateOpaqueToken(int numBytes) {
        byte[] bytes = new byte[numBytes];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Generates a zero-padded numeric OTP of the given number of digits. */
    public static String generateNumericOtp(int digits) {
        if (digits < 1 || digits > 9) {
            throw new IllegalArgumentException("OTP digits must be between 1 and 9");
        }
        int bound = (int) Math.pow(10, digits);
        int value = SECURE_RANDOM.nextInt(bound);
        return String.format("%0" + digits + "d", value);
    }

    /** Returns the lowercase hex SHA-256 hash of the supplied value. */
    public static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
