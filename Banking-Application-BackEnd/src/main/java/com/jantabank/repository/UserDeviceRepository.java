package com.jantabank.repository;

import com.jantabank.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    Optional<UserDevice> findByUserIdAndDeviceId(long userId, String deviceId);

    List<UserDevice> findByUserIdOrderByLastUsedAtDesc(long userId);

    Optional<UserDevice> findByIdAndUserId(long id, long userId);
}
