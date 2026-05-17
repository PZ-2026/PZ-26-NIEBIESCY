///*
// * DataControllerTest.java
// *
// * Version: 1.0
// * Date: 2026-05-03
// *
// * Copyright (c) 2026 EduLink Team. All rights reserved.
// *
// * This software is the confidential and proprietary information of EduLink.
// */
//
//package com.vectorpeaks.backend.controller;
//
//import com.vectorpeaks.backend.repository.SubjectRepository;
//import com.vectorpeaks.backend.repository.UserRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Unit tests for {@link DataController}.
// *
// * <p>Verifies the behaviour of the following endpoints:
// * <ul>
// *   <li>{@code GET /api/data/subjects} – retrieve distinct subject names,</li>
// *   <li>{@code GET /api/data/cities} – retrieve distinct city names.</li>
// * </ul>
// *
// * <p>Uses {@code @WebMvcTest} with {@link MockMvc} – only the controller layer
// * is loaded; no full Spring context or database is required.
// * The {@link SubjectRepository} and {@link UserRepository} dependencies are
// * replaced by Mockito mocks ({@code @MockitoBean}).
// *
// * @version 1.0
// * @author EduLink Team
// * @see DataController
// */
//@WebMvcTest(
//        controllers = DataController.class,
//        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
//)
//class DataControllerTest {
//
//    /** HTTP client used to perform requests in web-layer tests. */
//    @Autowired
//    private MockMvc mockMvc;
//
//    /** Mock of the subject repository – replaces the database layer. */
//    @MockitoBean
//    private SubjectRepository subjectRepository;
//
//    /** Mock of the user repository – replaces the database layer. */
//    @MockitoBean
//    private UserRepository userRepository;
//
//    // -----------------------------------------------------------------------
//    // GET /api/data/subjects
//    // -----------------------------------------------------------------------
//
//    /**
//     * Verifies that the endpoint returns {@code 200 OK} and a JSON array
//     * of subject names when subjects exist in the database.
//     *
//     * @throws Exception if the MockMvc request execution fails
//     */
//    @Test
//    void getSubjects_subjectsExist_returns200AndList() throws Exception {
//        when(subjectRepository.findAllDistinctNames())
//                .thenReturn(List.of("Mathematics", "Physics", "English"));
//
//        mockMvc.perform(get("/api/data/subjects"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(3))
//                .andExpect(jsonPath("$[0]").value("Mathematics"))
//                .andExpect(jsonPath("$[1]").value("Physics"))
//                .andExpect(jsonPath("$[2]").value("English"));
//    }
//
//    /**
//     * Verifies that the endpoint returns {@code 200 OK} and an empty JSON
//     * array when no subjects exist in the database.
//     *
//     * @throws Exception if the MockMvc request execution fails
//     */
//    @Test
//    void getSubjects_noSubjects_returns200AndEmptyList() throws Exception {
//        when(subjectRepository.findAllDistinctNames()).thenReturn(List.of());
//
//        mockMvc.perform(get("/api/data/subjects"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(0));
//    }
//
//    // -----------------------------------------------------------------------
//    // GET /api/data/cities
//    // -----------------------------------------------------------------------
//
//    /**
//     * Verifies that the endpoint returns {@code 200 OK} and a JSON array
//     * of city names when cities exist in the database.
//     *
//     * @throws Exception if the MockMvc request execution fails
//     */
//    @Test
//    void getCities_citiesExist_returns200AndList() throws Exception {
//        when(userRepository.findAllDistinctCities())
//                .thenReturn(List.of("Warszawa", "Kraków", "Gdańsk"));
//
//        mockMvc.perform(get("/api/data/cities"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(3))
//                .andExpect(jsonPath("$[0]").value("Warszawa"))
//                .andExpect(jsonPath("$[1]").value("Kraków"))
//                .andExpect(jsonPath("$[2]").value("Gdańsk"));
//    }
//
//    /**
//     * Verifies that the endpoint returns {@code 200 OK} and an empty JSON
//     * array when no cities are found in the database.
//     *
//     * @throws Exception if the MockMvc request execution fails
//     */
//    @Test
//    void getCities_noCities_returns200AndEmptyList() throws Exception {
//        when(userRepository.findAllDistinctCities()).thenReturn(List.of());
//
//        mockMvc.perform(get("/api/data/cities"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(0));
//    }
//}