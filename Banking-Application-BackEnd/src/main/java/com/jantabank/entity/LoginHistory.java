package com.jantabank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * An audit record of a single login attempt (successful or failed) capturing the
 * originating IP, user agent and device fingerprint for security review.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "login_history")
public class LoginHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "attempted_username", length = 150)
    private String attemptedUsername;

    @Column(name = "successful", nullable = false)
    private boolean successful;

    @Column(name = "failure_reason", length = 100)
    private String failureReason;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "device_id", length = 128)
    private String deviceId;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;
}
