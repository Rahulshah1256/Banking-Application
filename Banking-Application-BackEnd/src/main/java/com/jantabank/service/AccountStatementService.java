package com.jantabank.service;

import com.jantabank.dto.account.AccountDetailsDto;
import com.jantabank.dto.account.AccountSummaryDto;
import com.jantabank.dto.account.StatementItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Read-side operations for a single account: details, summary, transaction
 * statement and downloadable exports. All operations enforce that the caller
 * owns the account unless {@code admin} is true.
 */
public interface AccountStatementService {

    AccountDetailsDto getDetails(long accountId, String username, boolean admin);

    AccountSummaryDto getSummary(long accountId, String username, boolean admin);

    Page<StatementItemDto> getStatement(long accountId, String username, boolean admin,
                                        LocalDate from, LocalDate to, Pageable pageable);

    byte[] exportCsv(long accountId, String username, boolean admin, LocalDate from, LocalDate to);

    byte[] exportPdf(long accountId, String username, boolean admin, LocalDate from, LocalDate to);
}
