package com.jantabank.service;

import com.jantabank.dto.txn.ScheduleTransferRequest;
import com.jantabank.dto.txn.ScheduledTransferDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Manages standing instructions (scheduled and recurring transfers) and drives
 * their asynchronous execution.
 */
public interface ScheduledTransferService {

    ScheduledTransferDto schedule(ScheduleTransferRequest request, String username);

    Page<ScheduledTransferDto> listMine(String username, Pageable pageable);

    ScheduledTransferDto cancel(Long id, String username);

    /** Returns the ids of all instructions whose next run date is due. */
    java.util.List<Long> dueInstructionIds();

    /** Executes a single due instruction in its own transaction. */
    void executeInstruction(Long id);
}
