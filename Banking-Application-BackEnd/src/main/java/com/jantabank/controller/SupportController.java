package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.support.BranchLocatorResponse;
import com.jantabank.dto.support.CreateTicketRequest;
import com.jantabank.dto.support.FaqResponse;
import com.jantabank.dto.support.TicketMessageRequest;
import com.jantabank.dto.support.TicketResponse;
import com.jantabank.dto.support.UpdateTicketStatusRequest;
import com.jantabank.service.SupportService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/support")
@AllArgsConstructor
public class SupportController {

    private final SupportService supportService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/tickets")
    public ResponseEntity<ApiResponse<TicketResponse>> raiseTicket(
            @Valid @RequestBody CreateTicketRequest request, Principal principal) {
        TicketResponse response = supportService.raiseTicket(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(response, "Support ticket created"), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> listMine(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                supportService.listMine(principal.getName()), "Tickets retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/tickets/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(@PathVariable Long id,
                                                                 Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                supportService.getTicket(id, authentication.getName(), isAdmin(authentication)),
                "Ticket retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/tickets/{id}/messages")
    public ResponseEntity<ApiResponse<TicketResponse>> addMessage(@PathVariable Long id,
                                                                  @Valid @RequestBody TicketMessageRequest request,
                                                                  Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                supportService.addMessage(id, request, authentication.getName(), isAdmin(authentication)),
                "Message added"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/tickets/{id}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> updateStatus(@PathVariable Long id,
                                                                    @Valid @RequestBody UpdateTicketStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                supportService.updateStatus(id, request), "Ticket status updated"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/faqs")
    public ResponseEntity<ApiResponse<List<FaqResponse>>> faqs(
            @RequestParam(value = "category", required = false) String category) {
        return ResponseEntity.ok(ApiResponse.success(supportService.faqs(category), "FAQs retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<BranchLocatorResponse>>> branches(
            @RequestParam(value = "city", required = false) String city) {
        return ResponseEntity.ok(ApiResponse.success(supportService.branches(city), "Branches retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/atms")
    public ResponseEntity<ApiResponse<List<BranchLocatorResponse>>> atms(
            @RequestParam(value = "city", required = false) String city) {
        return ResponseEntity.ok(ApiResponse.success(supportService.atms(city), "ATMs retrieved"));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
