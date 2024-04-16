package com.jantabank.impl;

import com.jantabank.config.BranchConfig;
import com.jantabank.config.Enums;
import com.jantabank.dto.UserDto;
import com.jantabank.entity.Account;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {


    private UserRepository userRepository;

    private AccountRepository accountRepository;

    private ModelMapper modelMapper;

    private BranchConfig branchConfig;

    @Override
    public List<UserDto> getAllUserRequests() {
        List<User> users = userRepository.findUsersByStatus(Enums.AccountStatus.REQUESTED);

        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No accounts found");
        }
        return users.stream().map((user) -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto rejectRequest(long Id) {
        User user =  userRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " + Id));
        user.setStatus(Enums.AccountStatus.REJECTED);

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public UserDto generateAccount(long Id) {
        User user =  userRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " + Id));

            Set<User> users = new HashSet<>();
            users.add(user);

//             Perform account generation logic
            Account account = new Account();
            account.setAccountHolderName(user.getName());
            account.setAccountType(String.valueOf(Enums.AccountType.SAVINGS));
            account.setBranchId(branchConfig.getBranchId());
            account.setIfscCode(branchConfig.getBranchIfsc());
            account.setBalance(0.0);
            account.setAccountNumber("0");
            account.setOpenDate(new Date());
            account.setStatus(Enums.AccountStatus.ACTIVE);
            account.setAddress(user.getAddress());
            account.setContactNumber(user.getMobile());
            account.setEmailAddress(user.getEmail());
            account.setNominee(user.getName());
            // Establish the relationship with the user
            account.setUsers(users);
            Account newAccount = accountRepository.save(account);

            String accountNo = generateAccountNumber(branchConfig.getBranchId(),Enums.AccountType.SAVINGS
            ,newAccount.getId());
        newAccount.setAccountNumber(accountNo);
        Account updatedAccount = accountRepository.save(account);
        user.setStatus(Enums.AccountStatus.ACTIVE);

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    private String generateAccountNumber(String branchId,long accountType, long accountId) {
        // Implement logic to generate the account number
        // Example: Branch ID (4 characters) + Account Type (2 characters) + Account ID (6 digits, padded with zeros)
        String paddedAccountId = String.format("%06d", accountId); // Pad account ID with zeros
        String paddedAccountType = String.format("%02d", accountType); // Pad account ID with zeros
        return branchId + paddedAccountType + paddedAccountId;
    }
}
