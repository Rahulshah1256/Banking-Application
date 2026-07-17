package com.jantabank.impl;

import com.jantabank.dto.UserDeviceDto;
import com.jantabank.entity.User;
import com.jantabank.entity.UserDevice;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.repository.UserDeviceRepository;
import com.jantabank.service.DeviceService;
import com.jantabank.web.ClientMetadata;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceServiceImpl.class);

    private final UserDeviceRepository userDeviceRepository;

    @Override
    @Transactional
    public void registerOrUpdate(User user, ClientMetadata metadata) {
        if (metadata == null || metadata.getDeviceId() == null) {
            return;
        }
        UserDevice device = userDeviceRepository.findByUserIdAndDeviceId(user.getId(), metadata.getDeviceId())
                .orElseGet(() -> {
                    UserDevice created = new UserDevice();
                    created.setUser(user);
                    created.setDeviceId(metadata.getDeviceId());
                    created.setTrusted(false);
                    log.info("New device {} seen for userId={}", metadata.getDeviceId(), user.getId());
                    return created;
                });
        device.setDeviceName(metadata.getUserAgent());
        device.setIpAddress(metadata.getIpAddress());
        device.setLastUsedAt(LocalDateTime.now());
        userDeviceRepository.save(device);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDeviceDto> listDevices(long userId) {
        return userDeviceRepository.findByUserIdOrderByLastUsedAtDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeDevice(long userId, long deviceId) {
        UserDevice device = userDeviceRepository.findByIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        userDeviceRepository.delete(device);
        log.info("Device {} revoked for userId={}", deviceId, userId);
    }

    private UserDeviceDto toDto(UserDevice d) {
        return UserDeviceDto.builder()
                .id(d.getId())
                .deviceId(d.getDeviceId())
                .deviceName(d.getDeviceName())
                .ipAddress(d.getIpAddress())
                .lastUsedAt(d.getLastUsedAt())
                .trusted(d.isTrusted())
                .build();
    }
}
