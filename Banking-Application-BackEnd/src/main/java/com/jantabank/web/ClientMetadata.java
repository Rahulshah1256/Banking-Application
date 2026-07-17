package com.jantabank.web;

import com.jantabank.security.TokenHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * Immutable snapshot of client request metadata (IP, user agent, device
 * fingerprint) captured at the controller boundary and passed to auth services
 * for login history, device tracking and alerts.
 */
@Getter
public class ClientMetadata {

    private final String ipAddress;
    private final String userAgent;
    private final String deviceId;

    private ClientMetadata(String ipAddress, String userAgent, String deviceId) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceId = deviceId;
    }

    public static ClientMetadata from(HttpServletRequest request) {
        String ip = resolveIp(request);
        String userAgent = truncate(request.getHeader("User-Agent"), 512);

        String deviceId = request.getHeader("X-Device-Id");
        if (!StringUtils.hasText(deviceId)) {
            // Coarse fallback fingerprint derived from the user agent.
            String basis = StringUtils.hasText(userAgent) ? userAgent : "unknown";
            deviceId = "fp_" + TokenHashUtil.sha256Hex(basis).substring(0, 24);
        }
        deviceId = truncate(deviceId, 128);

        return new ClientMetadata(ip, userAgent, deviceId);
    }

    private static String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return truncate(forwarded.split(",")[0].trim(), 64);
        }
        return truncate(request.getRemoteAddr(), 64);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }
}
