package com.jantabank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A device from which a user has successfully authenticated, identified by an
 * opaque device fingerprint. Used for device management and new-device alerts.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_devices")
public class UserDevice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", nullable = false, length = 128)
    private String deviceId;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "last_used_at", nullable = false)
    private LocalDateTime lastUsedAt;

    @Column(name = "trusted", nullable = false)
    private boolean trusted;
}
