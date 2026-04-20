/*
 * AuthControllerTest.java
 *
 * Version: 1.1
 * Date: 2026-04-20
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorpeaks.backend.dto.LoginRequest;
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link AuthController}.
 *
 * <p>Weryfikuje zachowanie endpointu {@code POST /api/auth/login}, w tym:
 * <ul>
 *   <li>poprawną odpowiedź {@code 200 OK} z danymi użytkownika przy
 *       prawidłowych danych logowania,</li>
 *   <li>odpowiedź {@code 401 Unauthorized} przy błędnych danych,</li>
 *   <li>poprawny {@code Content-Type} odpowiedzi.</li>
 * </ul>
 *
 * <p>Używa {@code @WebMvcTest} z {@link MockMvc} – uruchamia tylko
 * warstwę kontrolera bez pełnego kontekstu Springa ani bazy danych.
 * Zależność {@link AuthService} jest zastąpiona mockiem Mockito
 * ({@code @MockitoBean}).
 *
 * @version 1.1
 * @author EduLink Team
 * @see AuthController
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    /** Klient HTTP do wykonywania żądań w testach warstwy web. */
    @Autowired
    private MockMvc mockMvc;

    /** Mock serwisu uwierzytelniania – zastępuje rzeczywistą implementację. */
    @MockitoBean
    private AuthService authService;

    /** Mapper JSON używany do serializacji obiektów żądań. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Przykładowy użytkownik inicjalizowany przed każdym testem. */
    private User mockUser;

    /**
     * Inicjalizuje przykładowego użytkownika zwracanego przez mock serwisu
     * przed każdym przypadkiem testowym.
     */
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(42);
        mockUser.setFirstName("Anna");
        mockUser.setLastName("Nowak");
        mockUser.setEmail("anna.nowak@example.com");
        mockUser.setRoleId(1);
    }

    // -----------------------------------------------------------------------
    // POST /api/auth/login – sukces
    // -----------------------------------------------------------------------

    /**
     * Weryfikuje, że endpoint zwraca status {@code 200 OK} oraz poprawne
     * dane użytkownika w formacie JSON gdy podane dane logowania są prawidłowe.
     *
     * @throws Exception jeśli wykonanie żądania MockMvc się nie powiedzie
     */
    @Test
    void login_poprawneCredentials_zwraca200iDaneUsera() throws Exception {
        when(authService.authenticate("anna.nowak@example.com", "tajne123"))
                .thenReturn(Optional.of(mockUser));

        LoginRequest request = new LoginRequest();
        request.setEmail("anna.nowak@example.com");
        request.setPassword("tajne123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.firstName").value("Anna"))
                .andExpect(jsonPath("$.lastName").value("Nowak"))
                .andExpect(jsonPath("$.email").value("anna.nowak@example.com"))
                .andExpect(jsonPath("$.role").value("1"));
    }

    // -----------------------------------------------------------------------
    // POST /api/auth/login – błąd autoryzacji
    // -----------------------------------------------------------------------

    /**
     * Weryfikuje, że endpoint zwraca status {@code 401 Unauthorized}
     * oraz komunikat o błędzie gdy podane dane logowania są nieprawidłowe.
     *
     * @throws Exception jeśli wykonanie żądania MockMvc się nie powiedzie
     */
    @Test
    void login_bledneCredentials_zwraca401() throws Exception {
        when(authService.authenticate(anyString(), anyString()))
                .thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("brak@example.com");
        request.setPassword("zleHaslo");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Nieprawidłowy email lub hasło"));
    }

    /**
     * Weryfikuje, że endpoint zwraca status {@code 401 Unauthorized}
     * gdy ciało żądania jest pustym obiektem JSON (brak e-maila i hasła).
     *
     * @throws Exception jeśli wykonanie żądania MockMvc się nie powiedzie
     */
    @Test
    void login_pustyCialoRequest_zwraca401() throws Exception {
        when(authService.authenticate(anyString(), anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Content-Type odpowiedzi
    // -----------------------------------------------------------------------

    /**
     * Weryfikuje, że odpowiedź przy udanym logowaniu ma nagłówek
     * {@code Content-Type} zgodny z {@code application/json}.
     *
     * @throws Exception jeśli wykonanie żądania MockMvc się nie powiedzie
     */
    @Test
    void login_sukces_odpowiedzJestJson() throws Exception {
        when(authService.authenticate(anyString(), anyString()))
                .thenReturn(Optional.of(mockUser));

        LoginRequest request = new LoginRequest();
        request.setEmail("anna.nowak@example.com");
        request.setPassword("tajne123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
