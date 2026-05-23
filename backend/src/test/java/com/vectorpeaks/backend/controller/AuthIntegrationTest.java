package com.vectorpeaks.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 * End-to-end integration tests for authentication workflows.
 * <p>
 * This class spins up the full Spring application context, incorporating the real
 * {@code SecurityConfig} without mocking the underlying authentication mechanisms.
 * </p>
 * <p>
 * <strong>Prerequisite:</strong> Since these are end-to-end tests connected to an actual
 * database instance (or a live test database), a user with the email {@code admin@edulink.com}
 * and password {@code hash1} must exist in the database prior to running these tests
 * (e.g., via database seed scripts or database migrations).
 * </p>
 *
 * @version 1.2
 * @author EduLink Team
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    /**
     * Main entry point for server-side Spring MVC test support.
     * Used to execute HTTP requests against the controllers.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper instance used to serialize Java request objects into JSON payloads.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * The email of the pre-existing user required to be present in the database.
     */
    private static final String EXISTING_EMAIL = "admin@edulink.com";

    /**
     * The correct password corresponding to the pre-existing test user.
     */
    private static final String CORRECT_PASSWD = "hash1";

    /**
     * Integration tests targeting the login endpoint.
     */
    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        /**
         * Verifies that providing valid credentials for an existing user in the database
         * successfully authenticates the user and returns a 200 OK status along with the
         * access tokens.
         *
         * @throws Exception if any error occurs during the MockMvc request execution
         */
        @Test
        @DisplayName("Poprawne dane logowania → 200 OK z tokenami")
        void login_existingUser_returns200() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail(EXISTING_EMAIL);
            request.setPassword(CORRECT_PASSWD);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(EXISTING_EMAIL))
                    .andExpect(jsonPath("$.token").exists());
        }

        /**
         * Verifies that providing an incorrect password for an existing user
         * correctly triggers authentication failure and returns a 401 Unauthorized status.
         *
         * @throws Exception if any error occurs during the MockMvc request execution
         */
        @Test
        @DisplayName("Niepoprawne hasło → 401")
        void login_invalidPassword_returns401() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail(EXISTING_EMAIL);
            request.setPassword("wrongPassword");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}