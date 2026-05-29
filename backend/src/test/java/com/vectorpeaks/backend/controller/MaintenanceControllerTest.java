/*
 * MaintenanceControllerTest.java
 *
 * Version: 1.0
 * Date: 2026-05-29
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link MaintenanceController}.
 *
 * <p>Verifies the behaviour of the following endpoints:
 * <ul>
 * <li>{@code GET /api/maintenance/status} – retrieves the current maintenance status,</li>
 * <li>{@code PUT /api/admin/maintenance} – toggles the maintenance mode (admin only).</li>
 * </ul>
 *
 * <p>Uses {@code @WebMvcTest} with {@link MockMvc}.
 * The {@code MaintenanceService} dependency is mocked and inherited from {@link BaseControllerTest}.
 *
 * @version 1.0
 * @author EduLink Team
 * @see MaintenanceController
 */
@WebMvcTest(
        controllers = MaintenanceController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
class MaintenanceControllerTest extends BaseControllerTest {

    /** HTTP client used to perform requests in web-layer tests. */
    @Autowired
    private MockMvc mockMvc;

    /** JSON mapper used to serialize request objects. */
    @Autowired
    private ObjectMapper objectMapper;

    // Note: maintenanceService is inherited from BaseControllerTest as a protected @MockitoBean.

    // -----------------------------------------------------------------------
    // GET /api/maintenance/status
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 200 OK} and correctly mapped JSON
     * when the system is actively in maintenance mode.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getMaintenanceStatus_whenActive_returnsCorrectStatus() throws Exception {
        LocalDateTime startTime = LocalDateTime.of(2026, 5, 29, 22, 0);

        when(maintenanceService.isActive()).thenReturn(true);
        when(maintenanceService.getStartsAt()).thenReturn(startTime);
        when(maintenanceService.isFullyActive()).thenReturn(false);

        mockMvc.perform(get("/api/maintenance/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.startsAt").value(startTime.toString()))
                .andExpect(jsonPath("$.fullyActive").value(false));
    }

    /**
     * Verifies that the endpoint returns {@code 200 OK} and correctly mapped JSON
     * when the system is NOT in maintenance mode.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getMaintenanceStatus_whenInactive_returnsCorrectStatus() throws Exception {
        when(maintenanceService.isActive()).thenReturn(false);
        when(maintenanceService.getStartsAt()).thenReturn(null);
        when(maintenanceService.isFullyActive()).thenReturn(false);

        mockMvc.perform(get("/api/maintenance/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.startsAt").isEmpty()) // checks for null in JSON
                .andExpect(jsonPath("$.fullyActive").value(false));
    }

    // -----------------------------------------------------------------------
    // PUT /api/admin/maintenance
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint calls {@code activate()} on the service and
     * returns {@code 200 OK} when a request with {"active": true} is provided.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void toggleMaintenance_activate_returns200AndCallsActivate() throws Exception {
        when(maintenanceService.isActive()).thenReturn(true);

        Map<String, Boolean> requestBody = Map.of("active", true);

        mockMvc.perform(put("/api/admin/maintenance")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        verify(maintenanceService, times(1)).activate();
    }

    /**
     * Verifies that the endpoint calls {@code deactivate()} on the service and
     * returns {@code 200 OK} when a request with {"active": false} is provided.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void toggleMaintenance_deactivate_returns200AndCallsDeactivate() throws Exception {
        when(maintenanceService.isActive()).thenReturn(false);

        Map<String, Boolean> requestBody = Map.of("active", false);

        mockMvc.perform(put("/api/admin/maintenance")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(maintenanceService, times(1)).deactivate();
    }

    /**
     * Verifies that the endpoint calls {@code forceActivate()} on the service and
     * returns {@code 200 OK} when {"force": true} is provided AND the service
     * is already active.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void toggleMaintenance_forceTrueWhenActive_returns200AndCallsForceActivate() throws Exception {
        // System is already active
        when(maintenanceService.isActive()).thenReturn(true);

        Map<String, Boolean> requestBody = Map.of("force", true);

        mockMvc.perform(put("/api/admin/maintenance")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        verify(maintenanceService, times(1)).forceActivate();
    }

    /**
     * Verifies that the endpoint returns {@code 400 Bad Request} when
     * {"force": true} is provided BUT the service is not currently active.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void toggleMaintenance_forceTrueWhenInactive_returns400() throws Exception {
        // System is inactive
        when(maintenanceService.isActive()).thenReturn(false);

        Map<String, Boolean> requestBody = Map.of("force", true);

        mockMvc.perform(put("/api/admin/maintenance")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

        verify(maintenanceService, never()).forceActivate();
    }

    /**
     * Verifies that the endpoint returns {@code 400 Bad Request} when
     * neither "active" nor "force" keys are provided in the payload.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void toggleMaintenance_missingFields_returns400() throws Exception {
        Map<String, Boolean> emptyBody = Map.of();

        mockMvc.perform(put("/api/admin/maintenance")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyBody)))
                .andExpect(status().isBadRequest());
    }
}