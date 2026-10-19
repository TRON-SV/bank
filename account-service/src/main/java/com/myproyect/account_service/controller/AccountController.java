package com.myproyect.account_service.controller;

import com.myproyect.account_service.domain.Account;
import com.myproyect.account_service.dto.AccountResponse;
import com.myproyect.account_service.dto.CreateAccountRequest;
import com.myproyect.account_service.mapper.AccountMapper;
import com.myproyect.account_service.service.AccountService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(
            AccountService accountService,
            AccountMapper accountMapper
    ) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {

        Account account = accountService.createAccount(
                request.customerId(),
                request.accountNumber(),
                request.currency()
        );

        return accountMapper.toDto(account);
    }
}