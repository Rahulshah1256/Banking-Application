package com.jantabank.controller;


import com.jantabank.dto.TransactionDto;
import com.jantabank.dto.UserDto;
import com.jantabank.service.TransactionService;
import com.jantabank.utils.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/transactions")
@AllArgsConstructor
public class TransactionController {

    private JwtUtils jwtUtils;
    private TransactionService transactionService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<TransactionDto> addTransaction(@RequestHeader(name="Authorization") String authorizationHeader,
                                                         @RequestBody TransactionDto transactionDto) {
        String jwtToken = authorizationHeader.substring("Bearer ".length());
        UserDto loggedInUserDto = jwtUtils.extractClaims(jwtToken);
        TransactionDto savedDto = transactionService.addTransaction(transactionDto,loggedInUserDto.getId());
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }
}
