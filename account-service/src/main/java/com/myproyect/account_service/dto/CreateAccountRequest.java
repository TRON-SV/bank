
package com.myproyect.account_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Define los datos de entrada para crear una cuenta y sus restricciones de validación.
 *
 * @param customerId identificador obligatorio del cliente
 * @param accountNumber número de cuenta no vacío, de hasta 20 caracteres
 * @param currency código de moneda compuesto por tres letras mayúsculas
 */
public record CreateAccountRequest(

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotBlank(message = "Account number is required")
        @Size(max = 20, message = "Account number cannot exceed 20 characters")
        String accountNumber,

        @NotNull(message = "Currency is required")
        @Pattern(
                regexp = "[A-Z]{3}",
                message = "Currency must contain exactly 3 uppercase letters"
        )
        String currency

) {
}
