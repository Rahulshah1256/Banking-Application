package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.dashboard.DashboardResponse;
import com.jantabank.service.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/dashboard")
@AllArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    //build customer dashboard REST API (authenticated)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(Principal principal) {
        DashboardResponse dashboard = dashboardService.getDashboard(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Dashboard retrieved"));
    }
}
