/*
 * AuthControllerTest.java
 *
 * Version: 1.2
 * Date: 2026-05-03
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
 * <p>Verifies the behaviour of the {@code POST /api/auth/login} endpoint, including:
 * <ul>
 *   <li>a {@code 200 OK} response with user data on valid credentials,</li>
 *   <li>a {@code 401 Unauthorized} response on invalid credentials,</li>
 *   <li>the correct {@code Content-Type} header in the response.</li>
 * </ul>
 *
 * <p>Uses {@code @WebMvcTest} with {@link MockMvc} – only the controller layer
 * is loaded; no full Spring context or database is required.
 * The {@link AuthService} dependency is replaced by a Mockito mock
 * ({@code @MockitoBean}).
 *
 * @version 1.2
 * @author EduLink Team
 * @see AuthController
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    /** HTTP client used to perform requests in web-layer tests. */
    @Autowired
    private MockMvc mockMvc;

    /** Mock of the authentication service – replaces the real implementation. */
    @MockitoBean
    private AuthService authService;

    /** JSON mapper used to serialize request objects. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Sample user initialised before each test. */
    private User mockUser;

    /**
     * Initialises a sample user returned by the mock service
     * before each test case.
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
    // POST /api/auth/login – success
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 200 OK} and correct user data
     * in JSON format when valid credentials are provided.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void login_validCredentials_returns200AndUserData() throws Exception {
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
    // POST /api/auth/login – authorisation error
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 401 Unauthorized} and an error
     * message when invalid credentials are provided.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.authenticate(anyString(), anyString()))
                .thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Nieprawidłowy email lub hasło"));
    }

    /**
     * Verifies that the endpoint returns {@code 401 Unauthorized}
     * when the request body is an empty JSON object (no email or password).
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void login_emptyRequestBody_returns401() throws Exception {
        when(authService.authenticate(anyString(), anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Response Content-Type
    // -----------------------------------------------------------------------

    /**
     * Verifies that a successful login response carries a
     * {@code Content-Type} header compatible with {@code application/json}.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void login_success_responseIsJson() throws Exception {
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