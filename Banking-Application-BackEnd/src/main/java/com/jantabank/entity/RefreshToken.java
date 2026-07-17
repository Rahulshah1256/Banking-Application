package com.jantabank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A persisted, revocable refresh token. Only the SHA-256 hash of the opaque
 * token value is stored, so a database compromise does not expose usable tokens.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

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

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
