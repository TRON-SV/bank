package com.myproyect.account_service;

import com.myproyect.account_service.config.SecurityConfig;
import com.myproyect.account_service.controller.AccountController;
import com.myproyect.account_service.domain.Account;
import com.myproyect.account_service.mapper.AccountMapper;
import com.myproyect.account_service.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.*;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Real Bearer filter, JWKS retrieval, RSA signature and claim validation.
@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, AccountMapper.class})
@ActiveProfiles("test")
class AccountSecurityTests {
    private static final RSAKey KEY;
    private static final HttpServer JWKS;
    static {
        try {
            KEY = new RSAKeyGenerator(2048).keyID("test-key").generate();
            JWKS = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            JWKS.createContext("/jwks", exchange -> {
                byte[] body = new JWKSet(KEY.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (var output = exchange.getResponseBody()) { output.write(body); }
            });
            JWKS.start();
        } catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "http://127.0.0.1:" + JWKS.getAddress().getPort() + "/jwks");
    }

    @AfterAll
    static void stopJwks() { JWKS.stop(0); }

    private static String token(String scope, String issuer, String audience, Instant expires, RSAKey key) throws Exception {
        var claims = new JWTClaimsSet.Builder().subject("test-client").issuer(issuer)
                .audience(audience).issueTime(Date.from(Instant.now().minusSeconds(600)))
                .expirationTime(Date.from(expires)).claim("scope", scope).build();
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("test-key").build(), claims);
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    private static String bearer(String scope) throws Exception {
        return "Bearer " + token(scope,
                "https://issuer.bank.test",
                "account-service",
                Instant.now().plusSeconds(300),
                KEY);
    }
    @Autowired
    MockMvc mvc;

    @MockitoBean
    AccountService service;

    private static final String BODY = """
            {"customerId":"d214c9bb-6717-43c7-a51f-a57d84519b10",
             "accountNumber":"ACC-SEC-001","currency":"USD"}
            """;

    @Test
    void anonymousCreationReturns401() throws Exception {
        mvc.perform(post("/api/v1/accounts").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void wrongScopeReturns403() throws Exception {
        mvc.perform(post("/api/v1/accounts").header("Authorization", bearer("accounts:read"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void createScopeReturns201WithoutCsrfToken() throws Exception {
        when(service.createAccount(any(), anyString(), anyString()))
                .thenAnswer(call -> new Account(call.getArgument(0), call.getArgument(1), call.getArgument(2)));
        mvc.perform(post("/api/v1/accounts").header("Authorization", bearer("accounts:create"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void otherRoutesRemainDeniedEvenWithCreateScope() throws Exception {
        mvc.perform(get("/api/v1/accounts").header("Authorization", bearer("accounts:create")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test
    void invalidTokensReturn401WithoutCallingService() throws Exception {
        RSAKey foreignKey = new RSAKeyGenerator(2048).generate();
        String[] invalid = {
            "malformed-token",
            token("accounts:create", "https://wrong.test", "account-service", Instant.now().plusSeconds(300), KEY),
            token("accounts:create", "https://issuer.bank.test", "other-service", Instant.now().plusSeconds(300), KEY),
            token("accounts:create", "https://issuer.bank.test", "account-service", Instant.now().minusSeconds(300), KEY),
            token("accounts:create", "https://issuer.bank.test", "account-service", Instant.now().plusSeconds(300), foreignKey)
        };
        for (String jwt : invalid) {
            mvc.perform(post("/api/v1/accounts").header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(service);
    }
}
