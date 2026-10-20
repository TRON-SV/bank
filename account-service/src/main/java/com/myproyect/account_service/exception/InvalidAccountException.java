package com.myproyect.account_service.exception;

/** Invalid account creation request detected in the business layer. */
public class InvalidAccountException extends IllegalArgumentException {

    public InvalidAccountException(String message) {
        super(message);
    }
}
