/*
 * BaseControllerTest.java
 *
 * Version: 1.2
 * Date: 2026-05-28
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.config.TestSecurityConfig;
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import com.vectorpeaks.backend.security.JwtAuthenticationFilter;
import com.vectorpeaks.backend.security.JwtUtil;
import com.vectorpeaks.backend.service.MaintenanceService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Base class for all {@code @WebMvcTest} controller tests.
 *
 * <p>Provides:
 * <ul>
 *   <li>{@link TestSecurityConfig}: disables CSRF and permits all requests,</li>
 *   <li>Mocks for JWT security components to isolate web layer testing,</li>
 *   <li>Bypass stubs for {@code AccessInterceptor} so it never blocks requests
 *       during tests – without these stubs every request would return
 *       {@code 403 "Konto zostało zablokowane."} because:
 *       <ol>
 *         <li>{@code MaintenanceService.isActive()} defaults to {@code false}
 *             on a raw mock, which is correct – but stubbed explicitly for clarity,</li>
 *         <li>{@code UserRepository.findById(principalId)} defaults to
 *             {@code Optional.empty()} on a raw mock, causing the interceptor to
 *             treat every user as blocked.</li>
 *       </ol>
 *   </li>
 * </ul>
 *
 * <p>Individual test classes may still declare their own {@code @MockitoBean}
 * fields for {@link MaintenanceService} and {@link UserRepository} when their
 * controller uses them directly. Because {@code @MockitoBean} beans are shared
 * across the test context for the same controller slice, the stubs set here in
 * {@code @BeforeEach} will apply to those shared instances automatically.
 *
 * @version 1.2
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
     * Mock of {@link MaintenanceService} required by {@code AccessInterceptor}.
     * Declared here so the bypass stub in {@link #setUpInterceptorBypass()} can
     * reference it without each subclass having to redeclare it.
     * Subclasses that do not declare their own field will use this one;
     * subclasses that need extra behaviour may override stubs in their own
     * {@code @BeforeEach}.
     */
    @MockitoBean
    protected MaintenanceService maintenanceService;

    /**
     * Mock of {@link UserRepository} required by {@code AccessInterceptor}
     * (constructor parameter 1). Declared here for the same reason as
     * {@link #maintenanceService}.
     */
    @MockitoBean
    protected UserRepository userRepository;

    /**
     * Configures the mocked JWT filter to behave as a pass-through component.
     * Prevents security filter chain invocation from swallowing or blocking
     * HTTP requests during test execution.
     *
     * <p>Also stubs {@code AccessInterceptor} dependencies so that the
     * interceptor never rejects test requests:
     * <ul>
     *   <li>{@code maintenanceService.isActive()} → {@code false}: system is
     *       not in maintenance mode.</li>
     *   <li>{@code userRepository.findById(any())} → active {@link User}:
     *       the account-blocked check always passes regardless of the principal
     *       ID used in {@code .with(authentication(...))} calls.</li>
     * </ul>
     *
     * @throws Exception if an error occurs during the servlet filter chain processing
     */
    @BeforeEach
    void setUpInterceptorBypass() throws Exception {
        // --- JWT filter pass-through ---
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        // --- AccessInterceptor: maintenance mode off ---
        when(maintenanceService.isActive()).thenReturn(false);

        User activeUser = new User();
        activeUser.setId(1);
        activeUser.setAccountStatusId(1); // 1 = active
        when(userRepository.findById(any())).thenReturn(Optional.of(activeUser));
    }
}