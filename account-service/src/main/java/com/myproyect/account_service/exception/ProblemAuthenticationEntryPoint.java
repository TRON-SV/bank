package com.myproyect.account_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Traduce fallos de autenticación en respuestas Problem Detail.
 *
 * <p>Indica el esquema Bearer mediante la cabecera WWW-Authenticate
 * y devuelve mensajes públicos sin incluir tokens ni detalles
 * internos de su validación.
 */
@Component
public class ProblemAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private static final Logger log =
            LoggerFactory.getLogger(ProblemAuthenticationEntryPoint.class);

    private final ApiProblemFactory problemFactory;
    private final SecurityProblemWriter problemWriter;

    public ProblemAuthenticationEntryPoint(
            ApiProblemFactory problemFactory,
            SecurityProblemWriter problemWriter) {

        this.problemFactory = problemFactory;
        this.problemWriter = problemWriter;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String code = "AUTHENTICATION_REQUIRED";
        String detail = "Authentication is required to access this resource";
        String challenge = "Bearer";

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String oauth2Code = oauth2Exception.getError().getErrorCode();

            if ("invalid_request".equals(oauth2Code)) {
                status = HttpStatus.BAD_REQUEST;
                code = "INVALID_AUTHENTICATION_REQUEST";
                detail = "The authentication request is malformed";
                challenge = "Bearer error=\"invalid_request\"";
            } else {
                code = "INVALID_TOKEN";
                detail = "The access token is invalid or expired";
                challenge = "Bearer error=\"invalid_token\"";
            }
        }

        log.debug(
                "Authentication rejected: status={}, code={}",
                status.value(),
                code
        );

        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, challenge);

        ProblemDetail problem =
                problemFactory.create(status, code, detail);

        problemWriter.write(response, problem);
    }
}