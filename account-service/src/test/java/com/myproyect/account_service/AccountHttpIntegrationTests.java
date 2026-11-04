package com.myproyect.account_service;

import com.myproyect.account_service.domain.Account;
import com.myproyect.account_service.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica la creación de cuentas y las respuestas de error a través de HTTP.
 *
 * <p>Utiliza PostgreSQL efímero mediante Testcontainers. Los filtros de seguridad
 * están desactivados para centrar estas pruebas en el flujo de negocio.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // ONLY for HTTP/business tests; NOT a Spring Security test.
@ActiveProfiles("test")
class AccountHttpIntegrationTests {

    @Autowired
    private AccountRepository accountRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn201WhenCreatingAccount() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ACC-HTTP-001", "USD")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountNumber").value("ACC-HTTP-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn400AndFieldErrorsForInvalidCurrency() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ACC-HTTP-002", "usd")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.currency").exists());
    }

    @Test
    void shouldReturn409ForDuplicateNumber() throws Exception {
        String body = body("ACC-HTTP-003", "USD");
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NUMBER_EXISTS"));
    }

    @Test
    void shouldReturn400ForMalformedBody() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void shouldReturn404WhenAccountIdDoesNotExist() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/accounts/" + unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
    }
    @Test
    void shouldReturnAccountById() throws Exception {
        Account account = accountRepository.saveAndFlush(
                new Account(UUID.randomUUID(), "ACC-GET-001", "USD")
        );

        mockMvc.perform(get("/api/v1/accounts/" + account.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId().toString()))
                .andExpect(jsonPath("$.accountNumber").value("ACC-GET-001"));
    }

    private static String body(String accountNumber, String currency) {
        return """
                {
                  "customerId":"%s",
                  "accountNumber":"%s",
                  "currency":"%s"
                }
                """.formatted(UUID.randomUUID(), accountNumber, currency);
    }
}
