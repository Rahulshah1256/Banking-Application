package com.jantabank.repository;

import com.jantabank.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByIdDesc(Long userId);

    List<Notification> findByUserIdAndReadFalseOrderByIdDesc(Long userId);

    long countByUserIdAndReadFalse(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
