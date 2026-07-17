package com.jantabank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Records the {@code jti} of an access token that has been explicitly revoked
 * (via logout) until the token's natural expiry, enabling true logout of an
 * otherwise stateless JWT.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "jti", nullable = false, unique = true)
    private String jti;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
}
