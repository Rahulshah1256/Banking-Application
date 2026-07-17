package com.jantabank.entity;

import com.jantabank.domain.enums.ScheduleFrequency;
import com.jantabank.domain.enums.ScheduleStatus;
import com.jantabank.domain.enums.TransferMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A standing instruction to move money on a future date, optionally repeating on
 * a fixed cadence. Executed asynchronously by {@code ScheduledTransferTask}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "scheduled_transfers")
public class ScheduledTransfer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", length = 40, nullable = false, unique = true)
    private String referenceNumber;

    @Column(name = "from_account_number", length = 50, nullable = false)
    private String fromAccountNumber;

    @Column(name = "to_account_number", length = 50, nullable = false)
    private String toAccountNumber;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_mode", length = 20, nullable = false)
    private TransferMode transferMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 20, nullable = false)
    private ScheduleFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ScheduleStatus status;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "next_run_date", nullable = false)
    private LocalDate nextRunDate;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "executions_count", nullable = false)
    private int executionsCount;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "initiated_by_user_id", nullable = false)
    private Long initiatedByUserId;
}
