package com.myproyect.account_service.exception;

/**
 * Indica que no se encontró la cuenta solicitada.
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException() {
        super("Account not found");
    }
}