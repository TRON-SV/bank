package com.myproyect.account_service.mapper;

import com.myproyect.account_service.domain.Account;
import com.myproyect.account_service.domain.AccountStatus;
import com.myproyect.account_service.dto.AccountResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica la conversión de cuentas a DTO y el tratamiento de una entidad nula.
 */
class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapper();

    @Test
    void shouldMapAccountToResponse() {
        UUID customerId = UUID.randomUUID();
        Account account = new Account(customerId, "ACC-MAP-001", "USD");

        AccountResponse response = mapper.toDto(account);

        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.accountNumber()).isEqualTo("ACC-MAP-001");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.status()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void shouldReturnNullForNullEntity() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
