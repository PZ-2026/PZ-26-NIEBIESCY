/*
 * UserControllerTest.java
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
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link UserController}.
 *
 * <p>Weryfikuje zachowanie endpointów:
 * <ul>
 *   <li>{@code GET /api/users} – pobieranie listy wszystkich użytkowników,</li>
 *   <li>{@code POST /api/users} – dodawanie nowego użytkownika.</li>
 * </ul>
 *
 * <p>Używa {@code @WebMvcTest} z {@link MockMvc} – uruchamia tylko
 * warstwę kontrolera bez pełnego kontekstu Springa ani bazy danych.
 * Zależność {@link UserRepository} jest zastąpiona mockiem Mockito
 * ({@code @MockitoBean}).
 *
 * @version 1.1
 * @author EduLink Team
 * @see UserController
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    /** Klient HTTP do wykonywania żądań w testach warstwy web. */
    @Autowired
    private MockMvc mockMvc;

    /** Mock repozytorium użytkowników – zastępuje warstwę bazodanową. */
    @MockitoBean
    private UserRepository userRepository;

    /** Mapper JSON używany do serializacji obiektów żądań. */
    @Autowired
    private ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // GET /api/users
    // -----------------------------------------------------------------------

    /**
     * Weryfikuje, że endpoint zwraca status {@code 200 OK} oraz tablicę JSON
     * zawierającą wszystkich użytkowników gdy w bazie istnieją rekordy.
     *
     * @throws Exception jeśli wykonanie żądania MockMvc się nie powiedzie
     */
    @Test
    void getAllUsers_listaUzytkownikow_zwraca200iJsonArray() throws Exception {
        User u1 = buildUser(1, "Jan",  "Kowalski", "jan@example.com",  1);
        User u2 = buildUser(2, "Anna", "Nowak",    "anna@example.com", 2);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Jan"))
                .andExpect(jsonPath("$[1].firstName").value("Anna"));
    }

    /**
     * Weryfikuje, że endpoint zwraca status {@code 200 OK} oraz pustą
     * tablicę JSON gdy w bazie nie ma żadnych użytkowników.
     *
     * @throws Exception jeśli wykonanie żądania MockMvc się nie powiedzie
     */
    @Test
    void getAllUsers_pustaLista_zwraca200iPustyArray() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -----------------------------------------------------------------------
    // POST /api/users
    // -----------------------------------------------------------------------

    /**
     * Weryfikuje, że endpoint zwraca status {@code 200 OK} oraz dane
     * zapisanego użytkownika (z wygenerowanym ID) w formacie JSON.
     *
     * @throws Exception jeśli wykonanie żądania MockMvc się nie powiedzie
     */
    @Test
    void addUser_poprawnyUser_zwraca200iZapisanegoUsera() throws Exception {
        User input = buildUser(null, "Piotr", "Wiśniewski", "piotr@example.com", 1);
        User saved  = buildUser(10,  "Piotr", "Wiśniewski", "piotr@example.com", 1);

        when(userRepository.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.firstName").value("Piotr"))
                .andExpect(jsonPath("$.email").value("piotr@example.com"));
    }

    /**
     * Weryfikuje, że odpowiedź po zapisaniu użytkownika zawiera poprawne
     * pole {@code roleId} zgodne z danymi wejściowymi.
     *
     * @throws Exception jeśli wykonanie żądania MockMvc się nie powiedzie
     */
    @Test
    void addUser_zwracaneDane_zawieraRoleId() throws Exception {
        User saved = buildUser(5, "Ewa", "Zielinska", "ewa@example.com", 3);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId").value(3));
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /**
     * Tworzy obiekt {@link User} z podanymi danymi – metoda pomocnicza
     * używana do budowania danych testowych.
     *
     * @param id      identyfikator użytkownika ({@code null} przed zapisem)
     * @param first   imię użytkownika
     * @param last    nazwisko użytkownika
     * @param email   adres e-mail użytkownika
     * @param roleId  identyfikator roli użytkownika
     * @return nowy obiekt {@link User} wypełniony podanymi danymi
     */
    private User buildUser(Integer id, String first, String last,
                           String email, Integer roleId) {
        User u = new User();
        u.setId(id);
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(email);
        u.setRoleId(roleId);
        return u;
    }
}
