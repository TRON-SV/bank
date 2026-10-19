
package com.myproyect.account_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

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
