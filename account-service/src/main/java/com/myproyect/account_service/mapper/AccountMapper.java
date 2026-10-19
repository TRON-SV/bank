package com.myproyect.account_service.mapper;

import com.myproyect.account_service.domain.Account;
import com.myproyect.account_service.dto.AccountResponse;

import org.springframework.stereotype.Component;

@Component
public class AccountMapper implements EntityMapper<Account, AccountResponse> {

    @Override
    public AccountResponse toDto(Account account) {

        if (account == null) {
            return null;
        }

        return new AccountResponse(
                account.getId(),
                account.getCustomerId(),
                account.getAccountNumber(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
