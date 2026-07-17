package com.jantabank.dto.notification;

import com.jantabank.domain.enums.NotificationChannel;
import com.jantabank.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private NotificationChannel channel;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
