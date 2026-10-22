package com.myproyect.account_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

/**
 * Construye objetos {@link ProblemDetail} para las respuestas de error de la API.
 *
 * <p>Establece el estado HTTP, el título, la descripción pública y el código de error.
 */
@Component
public class ApiProblemFactory {

    public ProblemDetail create(
            HttpStatus status,
            String code,
            String detail) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code);

        return problem;
    }
}
