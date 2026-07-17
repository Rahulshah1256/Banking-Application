package com.jantabank.controller;


import com.jantabank.common.ApiResponse;
import com.jantabank.domain.enums.BeneficiaryStatus;
import com.jantabank.dto.BeneficiaryDto;
import com.jantabank.dto.UserDto;
import com.jantabank.dto.beneficiary.BeneficiaryDetailDto;
import com.jantabank.dto.beneficiary.CreateBeneficiaryRequest;
import com.jantabank.dto.beneficiary.UpdateBeneficiaryRequest;
import com.jantabank.service.BeneficiaryManagementService;
import com.jantabank.service.BeneficiaryService;
import com.jantabank.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/beneficiaries")
@AllArgsConstructor
public class BeneficiaryController {

    private JwtUtils jwtUtils;
    private BeneficiaryService beneficiaryService;
    private BeneficiaryManagementService beneficiaryManagementService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<List<BeneficiaryDto>> getAllBeneficiaries(@RequestHeader(name="Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.substring("Bearer ".length());
        UserDto loggedInUserDto = jwtUtils.extractClaims(jwtToken);
        List<BeneficiaryDto> beneficiaries = beneficiaryService.getAllBeneficiaries(loggedInUserDto.getId());
        return ResponseEntity.ok(beneficiaries);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<BeneficiaryDto> addBeneficiary(@RequestHeader(name="Authorization") String authorizationHeader,
                                                         @RequestBody BeneficiaryDto beneficiaryDto) {
        String jwtToken = authorizationHeader.substring("Bearer ".length());
        UserDto loggedInUserDto = jwtUtils.extractClaims(jwtToken);
        BeneficiaryDto savedDto = beneficiaryService.addBeneficiary(beneficiaryDto,loggedInUserDto.getId());
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    //enriched beneficiary registration (activation-delay lifecycle)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<BeneficiaryDetailDto>> register(
            @Valid @RequestBody CreateBeneficiaryRequest request, Principal principal) {
        BeneficiaryDetailDto dto = beneficiaryManagementService.register(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success(dto, "Beneficiary registered"), HttpStatus.CREATED);
    }

    //update a beneficiary
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BeneficiaryDetailDto>> update(
            @PathVariable("id") long id, @Valid @RequestBody UpdateBeneficiaryRequest request, Principal principal) {
        BeneficiaryDetailDto dto = beneficiaryManagementService.update(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(dto, "Beneficiary updated"));
    }

    //delete a beneficiary
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable("id") long id, Principal principal) {
        beneficiaryManagementService.delete(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Beneficiary deleted"));
    }

    //approve/activate a beneficiary (admins bypass the activation delay)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<BeneficiaryDetailDto>> approve(@PathVariable("id") long id, Principal principal) {
        BeneficiaryDetailDto dto = beneficiaryManagementService.approve(id, principal.getName(), isAdmin());
        return ResponseEntity.ok(ApiResponse.success(dto, "Beneficiary approved"));
    }

    //toggle favourite flag
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("/{id}/favourite")
    public ResponseEntity<ApiResponse<BeneficiaryDetailDto>> toggleFavourite(
            @PathVariable("id") long id, Principal principal) {
        BeneficiaryDetailDto dto = beneficiaryManagementService.toggleFavourite(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(dto, "Beneficiary favourite toggled"));
    }

    //search/filter beneficiaries
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BeneficiaryDetailDto>>> search(
            @RequestParam(value = "status", required = false) BeneficiaryStatus status,
            @RequestParam(value = "favourite", required = false) Boolean favourite,
            @RequestParam(value = "q", required = false) String query,
            Principal principal) {
        List<BeneficiaryDetailDto> results = beneficiaryManagementService.search(
                principal.getName(), status, favourite, query);
        return ResponseEntity.ok(ApiResponse.success(results, "Beneficiaries retrieved"));
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

}
