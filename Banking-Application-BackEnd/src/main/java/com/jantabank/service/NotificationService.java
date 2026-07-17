package com.jantabank.service;

import com.jantabank.domain.enums.NotificationChannel;
import com.jantabank.domain.enums.NotificationType;
import com.jantabank.dto.notification.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse notify(Long userId, NotificationType type, NotificationChannel channel,
                                String title, String message);

    List<NotificationResponse> listMine(String username, boolean unreadOnly);

    long unreadCount(String username);

    NotificationResponse markRead(Long id, String username);

    int markAllRead(String username);

    void delete(Long id, String username);
}
