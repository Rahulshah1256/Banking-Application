package com.jantabank.repository;

import com.jantabank.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary,Long> {

    List<Beneficiary> findByAccounts_Id(long userId);

}
