package com.myproyect.account_service.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Traduce los rechazos de autorización en respuestas Problem Detail.
 *
 * <p>Devuelve un 403 con un mensaje público y utiliza los componentes comunes
 * para construir y escribir el cuerpo de la respuesta.
 */
@Component
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ProblemAccessDeniedHandler.class);

    private final ApiProblemFactory problemFactory;
    private final SecurityProblemWriter problemWriter;

    public ProblemAccessDeniedHandler(
            ApiProblemFactory problemFactory,
            SecurityProblemWriter problemWriter) {

        this.problemFactory = problemFactory;
        this.problemWriter = problemWriter;
    }

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AccessDeniedException exception)
            throws IOException, ServletException {

        log.debug("Authorization rejected: insufficient permissions");

        ProblemDetail problem = problemFactory.create(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to access this resource"
        );

        problemWriter.write(response, problem);
    }
}