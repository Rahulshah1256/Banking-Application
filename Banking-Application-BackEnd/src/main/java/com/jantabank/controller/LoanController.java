package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.loan.AmortizationEntry;
import com.jantabank.dto.loan.LoanApplicationRequest;
import com.jantabank.dto.loan.LoanCalculationRequest;
import com.jantabank.dto.loan.LoanCalculationResponse;
import com.jantabank.dto.loan.LoanPrepaymentRequest;
import com.jantabank.dto.loan.LoanRepaymentResponse;
import com.jantabank.dto.loan.LoanResponse;
import com.jantabank.service.LoanService;
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
@RequestMapping("api/loans")
@AllArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<LoanCalculationResponse>> calculate(
            @Valid @RequestBody LoanCalculationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(loanService.calculate(request), "EMI calculated"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<ApiResponse<LoanResponse>> apply(
            @Valid @RequestBody LoanApplicationRequest request, Principal principal) {
        LoanResponse response = loanService.apply(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(response, "Loan disbursed"), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanResponse>>> list(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(loanService.listMine(principal.getName()), "Loans retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanResponse>> get(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(loanService.get(id, principal.getName()), "Loan retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<List<AmortizationEntry>>> schedule(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(loanService.schedule(id, principal.getName()),
                "Amortization schedule retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}/statement")
    public ResponseEntity<ApiResponse<List<LoanRepaymentResponse>>> statement(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(loanService.statement(id, principal.getName()),
                "Loan statement retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/emi")
    public ResponseEntity<ApiResponse<LoanResponse>> payEmi(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(loanService.payEmi(id, principal.getName()), "EMI paid"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/prepay")
    public ResponseEntity<ApiResponse<LoanResponse>> prepay(@PathVariable Long id,
                                                            @Valid @RequestBody LoanPrepaymentRequest request,
                                                            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(loanService.prepay(id, request, principal.getName()),
                "Prepayment applied"));
    }
}
