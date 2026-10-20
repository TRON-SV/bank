package com.myproyect.account_service.service;

import com.myproyect.account_service.domain.Account;
import com.myproyect.account_service.exception.DuplicateAccountNumberException;
import com.myproyect.account_service.exception.InvalidAccountException;
import com.myproyect.account_service.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(UUID customerId, String accountNumber, String currency) {
        if (customerId == null) {
            throw new InvalidAccountException("Customer ID is required");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new InvalidAccountException("Account number is required");
        }

        String normalizedAccountNumber = accountNumber.strip();
        if (normalizedAccountNumber.length() > 20) {
            throw new InvalidAccountException("Account number cannot exceed 20 characters");
        }
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new InvalidAccountException("Currency must contain exactly 3 uppercase letters");
        }

        log.debug("Validating account number uniqueness");
        if (accountRepository.existsByAccountNumber(normalizedAccountNumber)) {
            throw new DuplicateAccountNumberException(normalizedAccountNumber);
        }

        Account account = new Account(customerId, normalizedAccountNumber, currency);
        // Flush detects DB constraints before the transactional service returns.
        // The UNIQUE index remains the final safeguard for concurrent requests.
        return accountRepository.saveAndFlush(account);
    }
}
