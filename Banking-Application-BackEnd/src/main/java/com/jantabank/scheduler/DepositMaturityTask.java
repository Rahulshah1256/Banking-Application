package com.jantabank.scheduler;

import com.jantabank.service.DepositService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodically processes matured deposits: credits the corpus back to the
 * linked account, or rolls it over when auto-renewal is enabled. Each deposit
 * is processed through the service proxy so it runs in its own REQUIRES_NEW
 * transaction and is isolated from sibling failures.
 */
@Component
@AllArgsConstructor
public class DepositMaturityTask {

    private static final Logger log = LoggerFactory.getLogger(DepositMaturityTask.class);

    private final DepositService depositService;

    @Scheduled(fixedRateString = "${app.deposit.scheduler-interval-milliseconds:300000}")
    public void processMaturedDeposits() {
        List<Long> dueIds = depositService.maturedDepositIds();
        if (dueIds.isEmpty()) {
            return;
        }
        log.info("Deposit-maturity sweep found {} matured deposit(s)", dueIds.size());
        for (Long id : dueIds) {
            try {
                depositService.processMaturity(id);
            } catch (Exception e) {
                log.error("Failed to process matured deposit id={}", id, e);
            }
        }
    }
}
