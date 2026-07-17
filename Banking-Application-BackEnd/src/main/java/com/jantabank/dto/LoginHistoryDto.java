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
public class LoginHistoryDto {
    private long id;
    private boolean successful;
    private String failureReason;
    private String ipAddress;
    private String userAgent;
    private String deviceId;
    private LocalDateTime loginTime;
}
