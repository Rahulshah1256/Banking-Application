package com.jantabank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A single-use, time-boxed password-reset token. Only the SHA-256 hash of the
 * opaque token value is persisted so a database leak cannot be used to reset
 * passwords.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "used", nullable = false)
    private boolean used;

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
