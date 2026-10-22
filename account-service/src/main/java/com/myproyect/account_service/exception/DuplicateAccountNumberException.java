package com.myproyect.account_service.exception;

/**
 * Indica que la creación de una cuenta se rechazó porque su número ya existe.
 */
public class DuplicateAccountNumberException extends RuntimeException {

    public DuplicateAccountNumberException() {
        super("Account number already exists");
    }
}