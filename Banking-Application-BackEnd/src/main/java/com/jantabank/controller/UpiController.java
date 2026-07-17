package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.txn.TransactionReceiptDto;
import com.jantabank.dto.upi.RegisterUpiHandleRequest;
import com.jantabank.dto.upi.UpiHandleDto;
import com.jantabank.dto.upi.UpiPayRequest;
import com.jantabank.dto.upi.UpiResolveDto;
import com.jantabank.service.UpiService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/upi")
@AllArgsConstructor
public class UpiController {

    private final UpiService upiService;

    //register a UPI VPA against one of the caller's accounts
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/handles")
    public ResponseEntity<ApiResponse<UpiHandleDto>> register(
            @Valid @RequestBody RegisterUpiHandleRequest request, Principal principal) {
        UpiHandleDto dto = upiService.registerHandle(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(dto, "VPA registered"), HttpStatus.CREATED);
    }

    //list the caller's VPAs
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/handles")
    public ResponseEntity<ApiResponse<List<UpiHandleDto>>> list(Principal principal) {
        List<UpiHandleDto> handles = upiService.listHandles(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(handles, "VPAs retrieved"));
    }

    //resolve a payee VPA to non-sensitive payee details
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/resolve/{vpa}")
    public ResponseEntity<ApiResponse<UpiResolveDto>> resolve(@PathVariable("vpa") String vpa) {
        UpiResolveDto dto = upiService.resolve(vpa);
        return ResponseEntity.ok(ApiResponse.success(dto, "VPA resolved"));
    }

    //pay a payee VPA
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<TransactionReceiptDto>> pay(
            @Valid @RequestBody UpiPayRequest request, Principal principal) {
        TransactionReceiptDto receipt = upiService.pay(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(receipt, "UPI payment completed"), HttpStatus.CREATED);
    }

    //generate a UPI collect QR (PNG) for one of the caller's VPAs
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/qr")
    public ResponseEntity<byte[]> qr(
            @RequestParam("vpa") String vpa,
            @RequestParam(value = "amount", required = false) Double amount,
            @RequestParam(value = "note", required = false) String note,
            Principal principal) {
        byte[] png = upiService.generateCollectQr(vpa, amount, note, principal.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}
