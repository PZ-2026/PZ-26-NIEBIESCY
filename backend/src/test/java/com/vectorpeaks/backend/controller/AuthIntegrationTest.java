/*
 * AuthIntegrationTest.java
 *
 * Version: 1.2
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorpeaks.backend.dto.LoginRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests for authentication.
 *
 * <p>Verifies the behaviour of the {@code POST /api/auth/login} endpoint in a full
 * application context, including:
 * <ul>
 * <li>a {@code 200 OK} response with user data on valid credentials,</li>
 * <li>a {@code 401 Unauthorized} response on invalid password,</li>
 * <li>a {@code 401 Unauthorized} response on non-existent user.</li>
 * </ul>
 *
 * <p>Uses {@code @SpringBootTest} and {@code @AutoConfigureMockMvc} to load the
 * complete Spring application context and test against a real or in-memory database.
 *
 * @version 1.2
 * @author EduLink Team
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    /** HTTP client used to perform requests in integration tests. */
    @Autowired
    private MockMvc mockMvc;

    /** JSON mapper used to serialize request objects. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Existing user email in the test database. */
    private static final String EXISTING_EMAIL = "admin@edulink.com";

    /** Correct password for the existing user. */
    private static final String CORRECT_PASSWD   = "hash1";

    // -----------------------------------------------------------------------
    // POST /api/auth/login
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 200 OK} and the correct email
     * in the JSON response when valid credentials of an existing user are provided.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void login_existingUser_returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(EXISTING_EMAIL);
        request.setPassword(CORRECT_PASSWD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EXISTING_EMAIL));
    }

    /**
     * Verifies that the endpoint returns {@code 401 Unauthorized}
     * when an existing user's email is provided with an incorrect password.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void login_invalidPassword_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(EXISTING_EMAIL);
        request.setPassword("zleHaslo999");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Verifies that the endpoint returns {@code 401 Unauthorized}
     * when a non-existent email address is provided.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void login_nonExistentUser_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("notexist@example.com");
        request.setPassword("whatever");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Intentionally tests a failing scenario where an invalid password is used,
     * but a {@code 200 OK} status is expected.
     *
     * @throws Exception if the MockMvc request execution fails
     */
//    @Test
//    void login_invalidPasswordButExpecting200_shouldFail() throws Exception {
//        LoginRequest request = new LoginRequest();
//        request.setEmail(EXISTING_EMAIL);
//        request.setPassword("zleHaslo999");
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//    }
}