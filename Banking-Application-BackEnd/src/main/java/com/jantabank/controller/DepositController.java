package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.deposit.AutoRenewRequest;
import com.jantabank.dto.deposit.DepositCalculationRequest;
import com.jantabank.dto.deposit.DepositCalculationResponse;
import com.jantabank.dto.deposit.DepositResponse;
import com.jantabank.dto.deposit.OpenFixedDepositRequest;
import com.jantabank.dto.deposit.OpenRecurringDepositRequest;
import com.jantabank.service.DepositService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/deposits")
@AllArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<DepositCalculationResponse>> calculate(
            @Valid @RequestBody DepositCalculationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(depositService.calculate(request), "Maturity calculated"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/fd")
    public ResponseEntity<ApiResponse<DepositResponse>> openFixed(
            @Valid @RequestBody OpenFixedDepositRequest request, Principal principal) {
        DepositResponse response = depositService.openFixed(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(response, "Fixed deposit opened"), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/rd")
    public ResponseEntity<ApiResponse<DepositResponse>> openRecurring(
            @Valid @RequestBody OpenRecurringDepositRequest request, Principal principal) {
        DepositResponse response = depositService.openRecurring(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(response, "Recurring deposit opened"), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepositResponse>>> list(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(depositService.listMine(principal.getName()), "Deposits retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepositResponse>> get(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(depositService.get(id, principal.getName()), "Deposit retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/installment")
    public ResponseEntity<ApiResponse<DepositResponse>> payInstallment(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(depositService.payInstallment(id, principal.getName()),
                "Installment paid"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<DepositResponse>> close(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(depositService.close(id, principal.getName()),
                "Deposit closed"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("/{id}/auto-renew")
    public ResponseEntity<ApiResponse<DepositResponse>> setAutoRenew(@PathVariable Long id,
                                                                     @Valid @RequestBody AutoRenewRequest request,
                                                                     Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(depositService.setAutoRenew(id, request, principal.getName()),
                "Auto-renewal updated"));
    }
}
