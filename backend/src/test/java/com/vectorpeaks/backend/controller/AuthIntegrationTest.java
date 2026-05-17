//package com.vectorpeaks.backend.controller;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.vectorpeaks.backend.dto.LoginRequest;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * End-to-end integration tests for authentication.
// * Uses FULL Spring Context with real SecurityConfig (not mocked).
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//class AuthIntegrationTest{
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private static final String EXISTING_EMAIL = "admin@edulink.com";
//    private static final String CORRECT_PASSWD = "hash1";
//
//    @Nested
//    @DisplayName("POST /api/auth/login")
//    class LoginTests {
//
//        @Test
//        @DisplayName("Poprawne dane logowania → 200 OK z tokenami")
//        void login_existingUser_returns200() throws Exception {
//            LoginRequest request = new LoginRequest();
//            request.setEmail(EXISTING_EMAIL);
//            request.setPassword(CORRECT_PASSWD);
//
//            mockMvc.perform(post("/api/auth/login")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.email").value(EXISTING_EMAIL))
//                    .andExpect(jsonPath("$.token").exists());
//        }
//
//        @Test
//        @DisplayName("Niepoprawne hasło → 401")
//        void login_invalidPassword_returns401() throws Exception {
//            LoginRequest request = new LoginRequest();
//            request.setEmail(EXISTING_EMAIL);
//            request.setPassword("wrongPassword");
//
//            mockMvc.perform(post("/api/auth/login")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isUnauthorized());
//        }
//    }
//}