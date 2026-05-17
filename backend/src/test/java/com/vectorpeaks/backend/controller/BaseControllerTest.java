package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.security.JwtAuthenticationFilter;
import com.vectorpeaks.backend.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Base class for @WebMvcTest controller tests.
 * Mocks all security dependencies to isolate the web layer.
 * Secured by default with a mocked user to avoid 403 Forbidden.
 */
@WithMockUser
public abstract class BaseControllerTest {

    @MockitoBean
    protected JwtUtil jwtUtil;

    @MockitoBean
    protected JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    protected UserDetailsService userDetailsService;
}