package com.jantabank.repository;

import com.jantabank.domain.enums.TransferMode;
import com.jantabank.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    @Query("SELECT t FROM Transaction t WHERE t.fromaccount IN :accounts OR t.toaccount IN :accounts ORDER BY t.transactiondate DESC")
    List<Transaction> findByAccountNumbers(@Param("accounts") Collection<String> accounts, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.fromaccount = :account OR t.toaccount = :account) AND t.transactiondate BETWEEN :from AND :to")
    Page<Transaction> findStatement(@Param("account") String account,
                                    @Param("from") Date from,
                                    @Param("to") Date to,
                                    Pageable pageable);

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    Page<Transaction> findByTransactiondateBetween(Date from, Date to, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t")
    double sumAllAmounts();

    @Query("SELECT t FROM Transaction t WHERE t.transactiondate BETWEEN :from AND :to")
    List<Transaction> findAllInRange(@Param("from") Date from, @Param("to") Date to);

    @Query("SELECT t FROM Transaction t WHERE (t.fromaccount IN :accounts OR t.toaccount IN :accounts) " +
            "AND t.transactiondate BETWEEN :from AND :to")
    List<Transaction> findByAccountsInRange(@Param("accounts") Collection<String> accounts,
                                            @Param("from") Date from,
                                            @Param("to") Date to);

    @Query("SELECT t FROM Transaction t WHERE (t.fromaccount IN :accounts OR t.toaccount IN :accounts) " +
            "AND t.transactiondate BETWEEN :from AND :to " +
            "AND (:mode IS NULL OR t.transferMode = :mode) " +
            "AND (:minAmount IS NULL OR t.amount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) " +
            "AND (:q IS NULL OR t.description LIKE CONCAT('%', :q, '%') OR t.fromaccount LIKE CONCAT('%', :q, '%') OR t.toaccount LIKE CONCAT('%', :q, '%') OR t.referenceNumber LIKE CONCAT('%', :q, '%'))")
    Page<Transaction> search(@Param("accounts") Collection<String> accounts,
                             @Param("from") Date from,
                             @Param("to") Date to,
                             @Param("mode") TransferMode mode,
                             @Param("minAmount") Double minAmount,
                             @Param("maxAmount") Double maxAmount,
                             @Param("q") String q,
                             Pageable pageable);
}
