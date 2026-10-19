package com.myproyect.account_service.dto;

import com.myproyect.account_service.domain.AccountStatus;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID customerId,
        String accountNumber,
        String currency,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
