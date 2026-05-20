/*
 * BaseControllerTest.java
 *
 * Version: 1.1
 * Date: 2026-05-18
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.config.TestSecurityConfig;
import com.vectorpeaks.backend.security.JwtAuthenticationFilter;
import com.vectorpeaks.backend.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Base class for all {@code @WebMvcTest} controller tests.
 *
 * <p>Provides:
 * <ul>
 * <li>{@link TestSecurityConfig}: disables CSRF and permits all requests,</li>
 * <li>Mocks for JWT security components to isolate web layer testing.</li>
 * </ul>
 *
 * @version 1.1
 * @author EduLink Team
 */
@Import(TestSecurityConfig.class)
public abstract class BaseControllerTest {

    /** Mocked utility for JWT token parsing and validation. */
    @MockitoBean
    protected JwtUtil jwtUtil;

    /** Mocked custom security filter executing token-based authentication. */
    @MockitoBean
    protected JwtAuthenticationFilter jwtAuthenticationFilter;

    /** Mocked standard service used to load user-specific security data. */
    @MockitoBean
    protected UserDetailsService userDetailsService;

    /**
     * Configures the mocked JWT filter to behave as a pass-through component.
     * Prevents security filter chain invocation from swallowing or blocking HTTP requests during test execution.
     *
     * @throws Exception if an error occurs during the servlet filter chain processing
     */
    @BeforeEach
    void setUpFilterPassThrough() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }
}