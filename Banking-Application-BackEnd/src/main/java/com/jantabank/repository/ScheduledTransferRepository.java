package com.jantabank.repository;

import com.jantabank.domain.enums.ScheduleStatus;
import com.jantabank.entity.ScheduledTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduledTransferRepository extends JpaRepository<ScheduledTransfer, Long> {

    Page<ScheduledTransfer> findByInitiatedByUserIdOrderByNextRunDateAsc(Long userId, Pageable pageable);

    Optional<ScheduledTransfer> findByIdAndInitiatedByUserId(Long id, Long userId);

    List<ScheduledTransfer> findByStatusAndNextRunDateLessThanEqual(ScheduleStatus status, LocalDate date);
}
