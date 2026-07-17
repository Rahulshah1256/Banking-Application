package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.AccountDto;
import com.jantabank.dto.UserDto;
import com.jantabank.dto.account.AccountDetailsDto;
import com.jantabank.dto.account.AccountSummaryDto;
import com.jantabank.dto.account.StatementItemDto;
import com.jantabank.service.AccountService;
import com.jantabank.service.AccountStatementService;
import com.jantabank.utils.JwtUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/accounts")
@AllArgsConstructor
public class AccountsController {
    private AccountService accountService;

    private AccountStatementService accountStatementService;

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

    //Build account details REST Api (owner or admin)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("{id}/details")
    public ResponseEntity<ApiResponse<AccountDetailsDto>> getAccountDetails(@PathVariable("id") long accountId,
                                                                            Principal principal) {
        AccountDetailsDto details = accountStatementService.getDetails(accountId, principal.getName(), isAdmin());
        return ResponseEntity.ok(ApiResponse.success(details, "Account details retrieved"));
    }

    //Build account summary REST Api (owner or admin)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("{id}/summary")
    public ResponseEntity<ApiResponse<AccountSummaryDto>> getAccountSummary(@PathVariable("id") long accountId,
                                                                            Principal principal) {
        AccountSummaryDto summary = accountStatementService.getSummary(accountId, principal.getName(), isAdmin());
        return ResponseEntity.ok(ApiResponse.success(summary, "Account summary retrieved"));
    }

    //Build paged account statement REST Api (owner or admin)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("{id}/statement")
    public ResponseEntity<ApiResponse<Page<StatementItemDto>>> getStatement(
            @PathVariable("id") long accountId,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<StatementItemDto> statement =
                accountStatementService.getStatement(accountId, principal.getName(), isAdmin(), from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success(statement, "Statement retrieved"));
    }

    //Build CSV statement download REST Api (owner or admin)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("{id}/statement/csv")
    public ResponseEntity<byte[]> downloadCsv(
            @PathVariable("id") long accountId,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {
        byte[] body = accountStatementService.exportCsv(accountId, principal.getName(), isAdmin(), from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"statement-" + accountId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    //Build PDF statement download REST Api (owner or admin)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("{id}/statement/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable("id") long accountId,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {
        byte[] body = accountStatementService.exportPdf(accountId, principal.getName(), isAdmin(), from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"statement-" + accountId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(body);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

}

