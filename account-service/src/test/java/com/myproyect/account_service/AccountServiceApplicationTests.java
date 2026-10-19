package com.myproyect.account_service;

import com.myproyect.account_service.domain.Account;
import com.myproyect.account_service.domain.AccountStatus;
import com.myproyect.account_service.exception.DuplicateAccountNumberException;
import com.myproyect.account_service.repository.AccountRepository;
import com.myproyect.account_service.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
class AccountServiceApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres =
			new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private AccountService accountService;

	@Test
	void contextLoads() {
		assertThat(accountRepository).isNotNull();
	}
	@Test
	void shouldSaveAndRetrieveAccount() {
		// Arrange: preparar una cuenta nueva.
		UUID customerId = UUID.randomUUID();
		String accountNumber = "ACC-TEST-001";

		Account account = new Account(
				customerId,
				accountNumber,
				"USD"
		);

		// Act: persistir la cuenta.
		Account savedAccount = accountRepository.saveAndFlush(account);

		// Assert: comprobar que se guardó correctamente.
		assertThat(savedAccount.getId()).isNotNull();
		assertThat(savedAccount.getCustomerId()).isEqualTo(customerId);
		assertThat(savedAccount.getAccountNumber()).isEqualTo(accountNumber);
		assertThat(savedAccount.getCurrency()).isEqualTo("USD");
		assertThat(savedAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(savedAccount.getCreatedAt()).isNotNull();
		assertThat(savedAccount.getUpdatedAt()).isNotNull();

		// Consultar la cuenta por su número.
		Account foundAccount = accountRepository
				.findByAccountNumber(accountNumber)
				.orElseThrow();

		assertThat(foundAccount.getId()).isEqualTo(savedAccount.getId());
		assertThat(foundAccount.getCustomerId()).isEqualTo(customerId);
	}
	@Test
	void shouldRejectDuplicateAccountNumber(){
		// Ambas cuentas tienen el mismo número.
		String accountNumber = "ACC-DUP-001";

		Account firstAccount = new Account(
				UUID.randomUUID(),
				accountNumber,
				"USD"
		);

		Account duplicateAccount = new Account(
				UUID.randomUUID(),
				accountNumber,
				"USD"
		);

		// Act: guardar la primera cuenta correctamente.
		accountRepository.saveAndFlush(firstAccount);

		// Assert: PostgreSQL debe rechazar la segunda cuenta.
		assertThatThrownBy(() ->
				accountRepository.saveAndFlush(duplicateAccount)
		).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void shouldCreateAccountThroughService() {
		UUID customerId = UUID.randomUUID();

		Account account = accountService.createAccount(
				customerId,
				"ACC-SVC-001",
				"USD"
		);

		assertThat(account.getId()).isNotNull();
		assertThat(account.getCustomerId()).isEqualTo(customerId);
		assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);

		assertThat(
				accountRepository.existsByAccountNumber("ACC-SVC-001")
		).isTrue();
	}

	@Test
	void shouldRejectDuplicateAccountNumberThroughService() {
		String accountNumber = "ACC-SVC-002";

		accountService.createAccount(
				UUID.randomUUID(),
				accountNumber,
				"USD"
		);

		assertThatThrownBy(() ->
				accountService.createAccount(
						UUID.randomUUID(),
						accountNumber,
						"USD"
				)
		).isInstanceOf(DuplicateAccountNumberException.class);
	}

	@Test
	void shouldRejectInvalidCurrency() {
		assertThatThrownBy(() ->
				accountService.createAccount(
						UUID.randomUUID(),
						"ACC-SVC-003",
						"usd"
				)
		).isInstanceOf(IllegalArgumentException.class);

		assertThat(
				accountRepository.existsByAccountNumber("ACC-SVC-003")
		).isFalse();//Must return false if the Account indeed was not created because invalid currency
	}

}
