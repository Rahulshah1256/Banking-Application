package com.jantabank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A one-time login OTP used for two-factor authentication. Only the SHA-256
 * hash of the numeric code is persisted; the code is short-lived and limited to
 * a fixed number of verification attempts.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "login_otps")
public class LoginOtp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "consumed", nullable = false)
    private boolean consumed;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
