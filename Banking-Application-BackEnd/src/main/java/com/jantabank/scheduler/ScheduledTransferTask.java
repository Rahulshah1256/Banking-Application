package com.jantabank.scheduler;

import com.jantabank.service.ScheduledTransferService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodically executes due scheduled/recurring transfer instructions. Each
 * instruction is processed through the service proxy so it runs in its own
 * REQUIRES_NEW transaction and is isolated from sibling failures.
 */
@Component
@AllArgsConstructor
public class ScheduledTransferTask {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTransferTask.class);

    private final ScheduledTransferService scheduledTransferService;

    @Scheduled(fixedRateString = "${app.transfer.scheduler-interval-milliseconds:300000}")
    public void executeDueTransfers() {
        List<Long> dueIds = scheduledTransferService.dueInstructionIds();
        if (dueIds.isEmpty()) {
            return;
        }
        log.info("Scheduled-transfer sweep found {} due instruction(s)", dueIds.size());
        for (Long id : dueIds) {
            try {
                scheduledTransferService.executeInstruction(id);
            } catch (Exception e) {
                log.error("Failed to execute scheduled transfer id={}", id, e);
            }
        }
    }
}
