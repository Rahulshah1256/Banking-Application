package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.domain.enums.TransferMode;
import com.jantabank.domain.enums.UserStatus;
import com.jantabank.dto.admin.AdminOverviewResponse;
import com.jantabank.dto.admin.AdminTransactionView;
import com.jantabank.dto.admin.AdminUserDetail;
import com.jantabank.dto.admin.AdminUserSummary;
import com.jantabank.dto.admin.UpdateUserStatusRequest;
import com.jantabank.dto.report.TransactionReportResponse;
import com.jantabank.service.AdminService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin("*")
@RestController
@RequestMapping("api/admin")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> overview() {
        return ResponseEntity.ok(ApiResponse.success(adminService.overview(), "Admin overview retrieved"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminUserSummary>>> listUsers(
            @RequestParam(value = "status", required = false) UserStatus status,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(ApiResponse.success(
                adminService.listUsers(status, query, pageable), "Users retrieved"));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminUserDetail>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserDetail(id), "User detail retrieved"));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<AdminUserSummary>> updateUserStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.updateUserStatus(id, request.getStatus()), "User status updated"));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<AdminTransactionView>>> transactions(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "mode", required = false) TransferMode mode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(ApiResponse.success(
                adminService.transactions(from, to, mode, pageable), "Transactions retrieved"));
    }

    @GetMapping("/reports/transactions")
    public ResponseEntity<ApiResponse<TransactionReportResponse>> transactionReport(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.transactionReport(from, to), "Transaction report generated"));
    }
}
