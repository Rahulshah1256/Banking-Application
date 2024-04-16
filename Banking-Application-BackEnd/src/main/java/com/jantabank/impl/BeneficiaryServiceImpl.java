package com.jantabank.impl;

import com.jantabank.dto.AccountDto;
import com.jantabank.dto.BeneficiaryDto;
import com.jantabank.entity.Account;
import com.jantabank.entity.Beneficiary;
import com.jantabank.repository.BeneficiaryRepository;
import com.jantabank.service.AccountService;
import com.jantabank.service.BeneficiaryService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private ModelMapper modelMapper;
    private BeneficiaryRepository beneficiaryRepository ;
    private AccountService accountService;

    @Override
    public BeneficiaryDto addBeneficiary(BeneficiaryDto beneficiaryDto,long userId) {

        List<AccountDto> accountDtos = accountService.getAccountByUserid(userId);
        List<Beneficiary> allBeneficiaries = new ArrayList<>();

        for (AccountDto accountDto : accountDtos) {
            List<Beneficiary> beneficiaries = beneficiaryRepository.findByAccounts_Id(accountDto.getId());
            allBeneficiaries.addAll(beneficiaries);
        }

        for (Beneficiary existingBeneficiary : allBeneficiaries) {
            if (existingBeneficiary.getBeneficiaryaccountnumber().equals(beneficiaryDto.getBeneficiaryaccountnumber())) {
                return null;
            }
        }

        beneficiaryDto.setStatus(1);
        List<Account> myAccounts = accountService.getAccountByUser(userId);
        Account selectedAccount = myAccounts.isEmpty() ? null : myAccounts.get(0);
        Set<Account> accounts = selectedAccount != null ? Collections.singleton(selectedAccount) : new HashSet<>();

        Beneficiary beneficiary = modelMapper.map(beneficiaryDto, Beneficiary.class);
        beneficiary.setAccounts((Set<Account>) accounts);

        //account jpa entity
        Beneficiary savedBeneficiary= beneficiaryRepository.save(beneficiary);
        BeneficiaryDto savedBeneficiaryDto = modelMapper.map(savedBeneficiary, BeneficiaryDto.class);
        return savedBeneficiaryDto;
    }

    @Override
    public List<BeneficiaryDto> getAllBeneficiaries(long userId) {

        List<AccountDto> accountDtos = accountService.getAccountByUserid(userId);
        List<Beneficiary> allBeneficiaries = new ArrayList<>();

        for (AccountDto accountDto : accountDtos) {
            List<Beneficiary> beneficiaries = beneficiaryRepository.findByAccounts_Id(accountDto.getId());
            allBeneficiaries.addAll(beneficiaries);
        }

        ModelMapper modelMapper = new ModelMapper();
        List<BeneficiaryDto> beneficiaryDtos = new ArrayList<>();
        for (Beneficiary beneficiary : allBeneficiaries) {
            BeneficiaryDto beneficiaryDto = modelMapper.map(beneficiary, BeneficiaryDto.class);
            beneficiaryDtos.add(beneficiaryDto);
        }

        return beneficiaryDtos;
    }
}
