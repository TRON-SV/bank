package com.myproyect.account_service.dto;

import com.myproyect.account_service.domain.AccountStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Define los datos de una cuenta expuestos en las respuestas de la API.
 *
 * @param id identificador de la cuenta
 * @param customerId identificador del cliente titular
 * @param accountNumber número de la cuenta
 * @param currency código de moneda
 * @param status estado de la cuenta
 * @param createdAt instante de creación
 * @param updatedAt instante de la última actualización
 */
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
