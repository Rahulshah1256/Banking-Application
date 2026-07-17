package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.card.BlockCardRequest;
import com.jantabank.dto.card.CardControlsRequest;
import com.jantabank.dto.card.CardDto;
import com.jantabank.dto.card.CardHistoryDto;
import com.jantabank.dto.card.CardLimitsRequest;
import com.jantabank.dto.card.IssueCardRequest;
import com.jantabank.dto.card.SetPinRequest;
import com.jantabank.service.CardService;
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
@RequestMapping("api/cards")
@AllArgsConstructor
public class CardController {

    private final CardService cardService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<ApiResponse<CardDto>> issue(@Valid @RequestBody IssueCardRequest request, Principal principal) {
        CardDto dto = cardService.issue(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(dto, "Card issued"), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CardDto>>> list(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(cardService.listMine(principal.getName()), "Cards retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CardDto>> get(@PathVariable long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(cardService.get(id, principal.getName()), "Card retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/block")
    public ResponseEntity<ApiResponse<CardDto>> block(@PathVariable long id,
                                                      @Valid @RequestBody(required = false) BlockCardRequest request,
                                                      Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(cardService.block(id, request, principal.getName()), "Card blocked"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<CardDto>> unblock(@PathVariable long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(cardService.unblock(id, principal.getName()), "Card unblocked"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/replace")
    public ResponseEntity<ApiResponse<CardDto>> replace(@PathVariable long id, Principal principal) {
        return new ResponseEntity<>(ApiResponse.success(cardService.replace(id, principal.getName()),
                "Card replaced"), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/pin")
    public ResponseEntity<ApiResponse<CardDto>> setPin(@PathVariable long id,
                                                       @Valid @RequestBody SetPinRequest request, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(cardService.setPin(id, request, principal.getName()), "PIN updated"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("/{id}/controls")
    public ResponseEntity<ApiResponse<CardDto>> controls(@PathVariable long id,
                                                         @Valid @RequestBody CardControlsRequest request,
                                                         Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(cardService.updateControls(id, request, principal.getName()),
                "Card controls updated"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("/{id}/limits")
    public ResponseEntity<ApiResponse<CardDto>> limits(@PathVariable long id,
                                                       @Valid @RequestBody CardLimitsRequest request,
                                                       Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(cardService.updateLimits(id, request, principal.getName()),
                "Card limits updated"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<CardHistoryDto>>> history(@PathVariable long id, Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(cardService.history(id, principal.getName()), "Card history retrieved"));
    }
}
