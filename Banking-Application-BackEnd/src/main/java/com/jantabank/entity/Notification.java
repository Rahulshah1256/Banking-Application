package com.jantabank.entity;

import com.jantabank.domain.enums.NotificationChannel;
import com.jantabank.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20, nullable = false)
    private NotificationChannel channel;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @Column(name = "message", length = 1000, nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
