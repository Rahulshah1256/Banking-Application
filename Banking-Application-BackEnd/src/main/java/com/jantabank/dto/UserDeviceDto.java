package com.jantabank.dto;

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
public class UserDeviceDto {
    private long id;
    private String deviceId;
    private String deviceName;
    private String ipAddress;
    private LocalDateTime lastUsedAt;
    private boolean trusted;
}
