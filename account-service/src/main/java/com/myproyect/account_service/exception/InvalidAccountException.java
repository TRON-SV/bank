package com.myproyect.account_service.exception;

/**
 * Indica que los datos de creación de una cuenta incumplen una regla de negocio.
 */
public class InvalidAccountException extends IllegalArgumentException {

    public InvalidAccountException(String message) {
        super(message);
    }
}
