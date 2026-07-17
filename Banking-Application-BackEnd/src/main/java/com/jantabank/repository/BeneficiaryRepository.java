package com.jantabank.repository;

import com.jantabank.domain.enums.BeneficiaryStatus;
import com.jantabank.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary,Long> {

    List<Beneficiary> findByAccounts_Id(long userId);

    Optional<Beneficiary> findByIdAndAccounts_Users_Id(long id, long userId);

    @Query("SELECT DISTINCT b FROM Beneficiary b JOIN b.accounts a JOIN a.users u " +
            "WHERE u.id = :userId " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:favourite IS NULL OR b.favourite = :favourite) " +
            "AND (:q IS NULL OR LOWER(b.beneficiaryaccountname) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "     OR b.beneficiaryaccountnumber LIKE CONCAT('%', :q, '%') " +
            "     OR LOWER(b.nickname) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY b.favourite DESC, b.id DESC")
    List<Beneficiary> search(@Param("userId") long userId,
                             @Param("status") BeneficiaryStatus status,
                             @Param("favourite") Boolean favourite,
                             @Param("q") String q);

    @Query("SELECT DISTINCT b FROM Beneficiary b JOIN b.accounts a JOIN a.users u " +
            "WHERE u.id = :userId AND b.beneficiaryaccountnumber = :accountNumber")
    List<Beneficiary> findOwnedByBeneficiaryAccountNumber(@Param("userId") long userId,
                                                          @Param("accountNumber") String accountNumber);
}
