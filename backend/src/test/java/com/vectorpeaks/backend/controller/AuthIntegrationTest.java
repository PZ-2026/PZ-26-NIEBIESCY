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

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Dane istniejącego użytkownika z Twojej bazy
    private static final String ISTNIEJACY_EMAIL = "admin@edulink.com";
    private static final String POPRAWNE_HASLO   = "hash1";

    @Test
    void login_istniejacyUser_zwraca200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(ISTNIEJACY_EMAIL);
        request.setPassword(POPRAWNE_HASLO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ISTNIEJACY_EMAIL));
    }

    @Test
    void login_zleHaslo_zwraca401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(ISTNIEJACY_EMAIL);
        request.setPassword("zleHaslo999");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_nieistniejacyUser_zwraca401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("nieistnieje@example.com");
        request.setPassword("cokolwiek");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    void login_zleHaslo_aleOczekujemy200_tenTestPowinienSieWysypac() throws Exception {
//        LoginRequest request = new LoginRequest();
//        request.setEmail(ISTNIEJACY_EMAIL);
//        request.setPassword("zleHaslo999"); // celowo złe hasło
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//    }
}
