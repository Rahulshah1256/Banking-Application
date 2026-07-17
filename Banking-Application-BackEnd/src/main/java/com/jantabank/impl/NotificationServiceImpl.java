package com.jantabank.impl;

import com.jantabank.domain.enums.NotificationChannel;
import com.jantabank.domain.enums.NotificationType;
import com.jantabank.dto.notification.NotificationResponse;
import com.jantabank.entity.Notification;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.repository.NotificationRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationResponse notify(Long userId, NotificationType type, NotificationChannel channel,
                                       String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setChannel(channel == null ? NotificationChannel.IN_APP : channel);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created id={} userId={} type={}", saved.getId(), userId, type);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> listMine(String username, boolean unreadOnly) {
        User user = loadUser(username);
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findByUserIdAndReadFalseOrderByIdDesc(user.getId())
                : notificationRepository.findByUserIdOrderByIdDesc(user.getId());
        return notifications.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(String username) {
        User user = loadUser(username);
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Override
    @Transactional
    public NotificationResponse markRead(Long id, String username) {
        User user = loadUser(username);
        Notification notification = notificationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Override
    @Transactional
    public int markAllRead(String username) {
        User user = loadUser(username);
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByIdDesc(user.getId());
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(n -> {
            n.setRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    @Override
    @Transactional
    public void delete(Long id, String username) {
        User user = loadUser(username);
        Notification notification = notificationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notificationRepository.delete(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .channel(n.getChannel())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
