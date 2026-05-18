/*
 * TestSecurityConfig.java
 *
 * Version: 1.1
 * Date: 2026-05-17
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test-only security configuration.
 * Disables CSRF and permits all requests so controller tests focus on
 * business logic, not security rules.
 *
 * <p>Import via {@code @Import(TestSecurityConfig.class)} in each test class.
 *
 * @version 1.1
 * @author EduLink Team
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Configures a simplified, permissive security filter chain for testing environments.
     * Bypasses standard authentication and authorization checks.
     *
     * @param http the {@link HttpSecurity} builder instance to modify
     * @return the constructed {@link SecurityFilterChain}
     * @throws Exception if an error occurs during HTTP security configuration
     */
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}