package com.myproyect.account_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.validation.FieldError;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidAccountException.class)
    public ResponseEntity<ProblemDetail> handleInvalidAccount(InvalidAccountException ex) {
        log.debug("Account creation rejected: invalid business input");
        return ResponseEntity.badRequest()
                .body(problem(HttpStatus.BAD_REQUEST, "INVALID_ACCOUNT", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateAccountNumberException.class)
    public ResponseEntity<ProblemDetail> handleDuplicate(DuplicateAccountNumberException ex) {
        log.info("Account creation rejected: duplicate number");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problem(HttpStatus.CONFLICT, "ACCOUNT_NUMBER_EXISTS",
                        "An account with this number already exists"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Database integrity constraint prevented account operation");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problem(HttpStatus.CONFLICT, "DATA_INTEGRITY_CONFLICT",
                        "The request conflicts with existing data constraints"));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ProblemDetail> handleDatabaseUnavailable(DataAccessResourceFailureException ex) {
        log.error("Database resource unavailable", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(problem(HttpStatus.SERVICE_UNAVAILABLE, "DATABASE_UNAVAILABLE",
                        "The service is temporarily unavailable"));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(),
                    fieldError.getDefaultMessage() == null
                            ? "Invalid value" : fieldError.getDefaultMessage());
        }

        log.debug("Request validation rejected for fields={}", fieldErrors.keySet());
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "One or more fields are invalid");
        detail.setProperty("fieldErrors", fieldErrors);
        return handleExceptionInternal(ex, detail, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.debug("Malformed or unreadable JSON request");
        return handleExceptionInternal(ex,
                problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Invalid JSON request body"),
                headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        log.error("Unhandled request error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                        "An unexpected error occurred"));
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail result = ProblemDetail.forStatusAndDetail(status, detail);
        result.setTitle(status.getReasonPhrase());
        result.setProperty("code", code);
        return result;
    }
}
