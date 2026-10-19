package com.myproyect.account_service.service;

import com.myproyect.account_service.domain.Account;
import com.myproyect.account_service.exception.DuplicateAccountNumberException;
import com.myproyect.account_service.repository.AccountRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(
            UUID customerId,
            String accountNumber,
            String currency
    ) {
        if (customerId == null) {
            throw new IllegalArgumentException(
                    "Customer ID is required"
            );
        }

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Account number is required"
            );
        }

        accountNumber = accountNumber.strip();

        if (accountNumber.length() > 20) {
            throw new IllegalArgumentException(
                    "Account number cannot exceed 20 characters"
            );
        }

        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException(
                    "Currency must contain exactly 3 uppercase letters"
            );
        }

        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new DuplicateAccountNumberException(accountNumber);
        }

        Account account = new Account(
                customerId,
                accountNumber,
                currency
        );

        return accountRepository.save(account);
    }
}
