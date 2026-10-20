package com.myproyect.account_service.controller;

import com.myproyect.account_service.domain.Account;
import com.myproyect.account_service.dto.AccountResponse;
import com.myproyect.account_service.dto.CreateAccountRequest;
import com.myproyect.account_service.mapper.EntityMapper;
import com.myproyect.account_service.service.AccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;
    private final EntityMapper<Account, AccountResponse> accountMapper;

    public AccountController(AccountService accountService,
                             EntityMapper<Account, AccountResponse> accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(
                request.customerId(), request.accountNumber(), request.currency());
        // createAccount's transaction completed before this line.
        log.info("Account created successfully accountId={}", account.getId());
        return accountMapper.toDto(account);
    }
}
