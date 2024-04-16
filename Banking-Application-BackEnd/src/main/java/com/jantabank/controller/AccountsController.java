package com.jantabank.controller;

import com.jantabank.dto.AccountDto;
import com.jantabank.dto.UserDto;
import com.jantabank.service.AccountService;
import com.jantabank.utils.JwtUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/accounts")
@AllArgsConstructor
public class AccountsController {
    private AccountService accountService;

    private JwtUtils jwtUtils;
    private static final Logger logger = LoggerFactory.getLogger(AccountsController.class);

    //Build add account REST Api
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AccountDto> addAccount(@RequestBody AccountDto accountDto) {
        AccountDto savedDto = accountService.addAccount(accountDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    //Build get by id account REST Api
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable("id") long accountid) {
        AccountDto savedDto = accountService.getAccount(accountid);
        return new ResponseEntity<>(savedDto, HttpStatus.OK);
    }

    //Build get all account REST Api
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<List<AccountDto>> getAllAccounts(@RequestHeader(name="Authorization") String authorizationHeader) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = ((User) authentication.getPrincipal());
        String jwtToken = authorizationHeader.substring("Bearer ".length());
        UserDto loggedInUserDto = jwtUtils.extractClaims(jwtToken);

        if (loggedInUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            List<AccountDto> accounts = accountService.getAllAccounts();
            return ResponseEntity.ok(accounts);
        }
        else
        {
            List<AccountDto> accounts = accountService.getAccountByUserid(loggedInUserDto.getId());
            return ResponseEntity.ok(accounts);
        }
    }

    //Build delete  account REST Api
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable("id") Long accountid)
    {
        accountService.deleteAccount(accountid);
        return ResponseEntity.ok("Deleted Successfully!");
    }

}

