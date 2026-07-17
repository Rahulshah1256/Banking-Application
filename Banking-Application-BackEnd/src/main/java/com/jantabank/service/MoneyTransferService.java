package com.jantabank.service;

import com.jantabank.domain.enums.TransactionType;
import com.jantabank.domain.enums.TransferMode;
import com.jantabank.dto.txn.TransactionReceiptDto;
import com.jantabank.dto.txn.TransactionSummaryDto;
import com.jantabank.dto.txn.TransferRequest;
import com.jantabank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Enriched money-movement operations: mode-aware transfers, searchable history,
 * transaction detail and downloadable receipts. All operations are scoped to the
 * authenticated customer's own accounts.
 */
public interface MoneyTransferService {

    TransactionReceiptDto transfer(TransferRequest request, String username);

    /**
     * Executes the core debit/credit for an already-resolved initiator. Shared by
     * the interactive transfer path and the scheduled-transfer engine.
     */
    TransactionReceiptDto executeTransfer(TransferRequest request, User initiator,
                                          String channel, TransactionType transactionType);

    Page<TransactionSummaryDto> getHistory(String username, TransferMode mode, Double minAmount,
                                           Double maxAmount, String query, LocalDate from, LocalDate to,
                                           Pageable pageable);

    TransactionReceiptDto getByReference(String referenceNumber, String username);

    byte[] generateReceiptPdf(String referenceNumber, String username);
}
