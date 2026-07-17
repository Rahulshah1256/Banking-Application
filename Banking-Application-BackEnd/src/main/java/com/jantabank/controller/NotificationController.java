package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.notification.NotificationResponse;
import com.jantabank.dto.notification.UnreadCountResponse;
import com.jantabank.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/notifications")
@AllArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> list(
            @RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.listMine(principal.getName(), unreadOnly), "Notifications retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> unreadCount(Principal principal) {
        long count = notificationService.unreadCount(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(
                UnreadCountResponse.builder().unreadCount(count).build(), "Unread count retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.markRead(id, principal.getName()), "Notification marked as read"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> markAllRead(Principal principal) {
        int updated = notificationService.markAllRead(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(
                UnreadCountResponse.builder().unreadCount(updated).build(),
                updated + " notification(s) marked as read"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id, Principal principal) {
        notificationService.delete(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", "Notification deleted"));
    }
}
