package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.profile.NomineeRequest;
import com.jantabank.dto.profile.NomineeResponse;
import com.jantabank.service.NomineeService;
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
@RequestMapping("api/nominees")
@AllArgsConstructor
public class NomineeController {

    private final NomineeService nomineeService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<ApiResponse<NomineeResponse>> add(@Valid @RequestBody NomineeRequest request,
                                                            Principal principal) {
        NomineeResponse response = nomineeService.add(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(response, "Nominee added"), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NomineeResponse>>> list(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(nomineeService.listMine(principal.getName()), "Nominees retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NomineeResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody NomineeRequest request,
                                                               Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(nomineeService.update(id, request, principal.getName()),
                "Nominee updated"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id, Principal principal) {
        nomineeService.delete(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Nominee deleted"));
    }
}
