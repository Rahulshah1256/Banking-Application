package com.jantabank.service;

import com.jantabank.dto.UserDeviceDto;
import com.jantabank.entity.User;
import com.jantabank.web.ClientMetadata;

import java.util.List;

public interface DeviceService {

    /** Registers a newly seen device or updates the last-used details of a known one. */
    void registerOrUpdate(User user, ClientMetadata metadata);

    /** Lists a user's known devices, most recently used first. */
    List<UserDeviceDto> listDevices(long userId);

    /** Removes (de-authorizes) one of the user's devices. */
    void revokeDevice(long userId, long deviceId);
}
