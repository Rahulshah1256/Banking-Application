package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.domain.enums.ChequeStatus;
import com.jantabank.dto.cheque.ChequeBookRequest;
import com.jantabank.dto.cheque.ChequeBookResponse;
import com.jantabank.dto.cheque.ChequeResponse;
import com.jantabank.dto.cheque.PositivePayRequest;
import com.jantabank.dto.cheque.StopChequeRequest;
import com.jantabank.service.ChequeService;
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
@RequestMapping("api/cheques")
@AllArgsConstructor
public class ChequeController {

    private final ChequeService chequeService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/books")
    public ResponseEntity<ApiResponse<ChequeBookResponse>> requestBook(
            @Valid @RequestBody ChequeBookRequest request, Principal principal) {
        ChequeBookResponse response = chequeService.requestBook(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(response, "Cheque book requested"), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/books")
    public ResponseEntity<ApiResponse<List<ChequeBookResponse>>> listBooks(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(chequeService.listBooks(principal.getName()),
                "Cheque books retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/books/{id}")
    public ResponseEntity<ApiResponse<ChequeBookResponse>> getBook(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(chequeService.getBook(id, principal.getName()),
                "Cheque book retrieved"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/books/{id}/issue")
    public ResponseEntity<ApiResponse<ChequeBookResponse>> issueBook(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(chequeService.issueBook(id), "Cheque book issued"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/books/{id}/deliver")
    public ResponseEntity<ApiResponse<ChequeBookResponse>> deliverBook(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(chequeService.deliverBook(id), "Cheque book delivered"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/leaves")
    public ResponseEntity<ApiResponse<List<ChequeResponse>>> history(
            @RequestParam(value = "status", required = false) ChequeStatus status,
            @RequestParam(value = "accountNumber", required = false) String accountNumber,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                chequeService.history(principal.getName(), status, accountNumber), "Cheque history retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/leaves/{id}")
    public ResponseEntity<ApiResponse<ChequeResponse>> getCheque(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(chequeService.getCheque(id, principal.getName()),
                "Cheque retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/leaves/{id}/stop")
    public ResponseEntity<ApiResponse<ChequeResponse>> stopCheque(@PathVariable Long id,
                                                                  @Valid @RequestBody StopChequeRequest request,
                                                                  Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(chequeService.stopCheque(id, request, principal.getName()),
                "Cheque stopped"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/leaves/{id}/positive-pay")
    public ResponseEntity<ApiResponse<ChequeResponse>> positivePay(@PathVariable Long id,
                                                                   @Valid @RequestBody PositivePayRequest request,
                                                                   Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                chequeService.registerPositivePay(id, request, principal.getName()), "Positive pay registered"));
    }
}
