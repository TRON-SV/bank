package com.myproyect.account_service.exception;

public class DuplicateAccountNumberException extends RuntimeException {

    public DuplicateAccountNumberException() {
        super("Account number already exists");
    }
}