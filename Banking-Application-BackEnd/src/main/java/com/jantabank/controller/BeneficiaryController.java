package com.jantabank.controller;


import com.jantabank.dto.BeneficiaryDto;
import com.jantabank.dto.UserDto;
import com.jantabank.service.BeneficiaryService;
import com.jantabank.utils.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/beneficiaries")
@AllArgsConstructor
public class BeneficiaryController {

    private JwtUtils jwtUtils;
    private BeneficiaryService beneficiaryService;

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

}
