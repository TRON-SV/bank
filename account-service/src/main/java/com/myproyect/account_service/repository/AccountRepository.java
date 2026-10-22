package com.myproyect.account_service.repository;

import com.myproyect.account_service.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Define las operaciones de persistencia de cuentas mediante Spring Data JPA.
 *
 * <p>Permite buscar una cuenta por su número y comprobar si ese número ya existe.
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);
}
