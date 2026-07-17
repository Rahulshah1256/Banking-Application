package com.jantabank.controller;


import com.jantabank.common.ApiResponse;
import com.jantabank.domain.enums.TransferMode;
import com.jantabank.dto.TransactionDto;
import com.jantabank.dto.UserDto;
import com.jantabank.dto.txn.ScheduleTransferRequest;
import com.jantabank.dto.txn.ScheduledTransferDto;
import com.jantabank.dto.txn.TransactionReceiptDto;
import com.jantabank.dto.txn.TransactionSummaryDto;
import com.jantabank.dto.txn.TransferRequest;
import com.jantabank.service.MoneyTransferService;
import com.jantabank.service.ScheduledTransferService;
import com.jantabank.service.TransactionService;
import com.jantabank.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@CrossOrigin("*")
@RestController
@RequestMapping("api/transactions")
@AllArgsConstructor
public class TransactionController {

    private JwtUtils jwtUtils;
    private TransactionService transactionService;
    private MoneyTransferService moneyTransferService;
    private ScheduledTransferService scheduledTransferService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<TransactionDto> addTransaction(@RequestHeader(name="Authorization") String authorizationHeader,
                                                         @RequestBody TransactionDto transactionDto) {
        String jwtToken = authorizationHeader.substring("Bearer ".length());
        UserDto loggedInUserDto = jwtUtils.extractClaims(jwtToken);
        TransactionDto savedDto = transactionService.addTransaction(transactionDto,loggedInUserDto.getId());
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    //build enriched, mode-aware transfer REST API
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionReceiptDto>> transfer(@Valid @RequestBody TransferRequest request,
                                                                       Principal principal) {
        TransactionReceiptDto receipt = moneyTransferService.transfer(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(receipt, "Transfer completed"), HttpStatus.CREATED);
    }

    //build transaction history/search REST API (paged, filterable)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionSummaryDto>>> getHistory(
            @RequestParam(value = "mode", required = false) TransferMode mode,
            @RequestParam(value = "minAmount", required = false) Double minAmount,
            @RequestParam(value = "maxAmount", required = false) Double maxAmount,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<TransactionSummaryDto> history = moneyTransferService.getHistory(
                principal.getName(), mode, minAmount, maxAmount, query, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success(history, "Transaction history retrieved"));
    }

    //build transaction detail REST API
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<TransactionReceiptDto>> getTransaction(@PathVariable("reference") String reference,
                                                                             Principal principal) {
        TransactionReceiptDto detail = moneyTransferService.getByReference(reference, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(detail, "Transaction retrieved"));
    }

    //build transaction receipt PDF REST API
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{reference}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable("reference") String reference, Principal principal) {
        byte[] body = moneyTransferService.generateReceiptPdf(reference, principal.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"receipt-" + reference + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(body);
    }

    //build scheduled/recurring transfer REST API
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/scheduled")
    public ResponseEntity<ApiResponse<ScheduledTransferDto>> schedule(
            @Valid @RequestBody ScheduleTransferRequest request, Principal principal) {
        ScheduledTransferDto dto = scheduledTransferService.schedule(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(dto, "Transfer scheduled"), HttpStatus.CREATED);
    }

    //build list scheduled transfers REST API (paged)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/scheduled")
    public ResponseEntity<ApiResponse<Page<ScheduledTransferDto>>> listScheduled(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<ScheduledTransferDto> scheduled = scheduledTransferService.listMine(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(scheduled, "Scheduled transfers retrieved"));
    }

    //build cancel scheduled transfer REST API
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("/scheduled/{id}")
    public ResponseEntity<ApiResponse<ScheduledTransferDto>> cancelScheduled(
            @PathVariable("id") Long id, Principal principal) {
        ScheduledTransferDto dto = scheduledTransferService.cancel(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(dto, "Scheduled transfer cancelled"));
    }
}
