package com.jantabank.repository;

import com.jantabank.domain.enums.ChequeStatus;
import com.jantabank.entity.Cheque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChequeRepository extends JpaRepository<Cheque, Long> {

    List<Cheque> findByChequeBookIdOrderByChequeNumberAsc(Long chequeBookId);

    Optional<Cheque> findByIdAndUserId(Long id, Long userId);

    boolean existsByChequeNumber(long chequeNumber);

    @Query("SELECT c FROM Cheque c WHERE c.userId = :userId "
            + "AND (:status IS NULL OR c.status = :status) "
            + "AND (:accountNumber IS NULL OR c.accountNumber = :accountNumber) "
            + "ORDER BY c.chequeNumber ASC")
    List<Cheque> search(@Param("userId") Long userId,
                        @Param("status") ChequeStatus status,
                        @Param("accountNumber") String accountNumber);
}
