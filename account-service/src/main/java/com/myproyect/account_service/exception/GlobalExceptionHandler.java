package com.myproyect.account_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce excepciones de negocio, validación e infraestructura en respuestas HTTP.
 *
 * <p>Centraliza su registro y utiliza {@link ApiProblemFactory} para construir
 * los cuerpos de error con un formato común.
 */
@RestControllerAdvice
public class GlobalExceptionHandler
        extends ResponseEntityExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ApiProblemFactory problemFactory;

    public GlobalExceptionHandler(ApiProblemFactory problemFactory) {
        this.problemFactory = problemFactory;
    }

    @ExceptionHandler(InvalidAccountException.class)
    public ResponseEntity<ProblemDetail> handleInvalidAccount(
            InvalidAccountException ex) {

        log.debug("Account creation rejected: invalid business input");

        return ResponseEntity.badRequest()
                .body(problemFactory.create(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_ACCOUNT",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(DuplicateAccountNumberException.class)
    public ResponseEntity<ProblemDetail> handleDuplicate(
            DuplicateAccountNumberException ex) {

        log.info("Account creation rejected: duplicate number");

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problemFactory.create(
                        HttpStatus.CONFLICT,
                        "ACCOUNT_NUMBER_EXISTS",
                        "An account with this number already exists"
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleIntegrity(
            DataIntegrityViolationException ex) {

        log.warn("Database integrity constraint prevented account operation");

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problemFactory.create(
                        HttpStatus.CONFLICT,
                        "DATA_INTEGRITY_CONFLICT",
                        "The request conflicts with existing data constraints"
                ));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ProblemDetail> handleDatabaseUnavailable(
            DataAccessResourceFailureException ex) {

        log.error("Database resource unavailable", ex);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(problemFactory.create(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "DATABASE_UNAVAILABLE",
                        "The service is temporarily unavailable"
                ));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError
                : ex.getBindingResult().getFieldErrors()) {

            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() == null
                            ? "Invalid value"
                            : fieldError.getDefaultMessage()
            );
        }

        log.debug(
                "Request validation rejected for fields={}",
                fieldErrors.keySet()
        );

        ProblemDetail problem = problemFactory.create(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "One or more fields are invalid"
        );

        problem.setProperty("fieldErrors", fieldErrors);

        return handleExceptionInternal(
                ex, problem, headers, status, request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.debug("Malformed or unreadable JSON request");

        ProblemDetail problem = problemFactory.create(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "Invalid JSON request body"
        );

        return handleExceptionInternal(
                ex, problem, headers, status, request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {

        log.error("Unhandled request error", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problemFactory.create(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_ERROR",
                        "An unexpected error occurred"
                ));
    }
}