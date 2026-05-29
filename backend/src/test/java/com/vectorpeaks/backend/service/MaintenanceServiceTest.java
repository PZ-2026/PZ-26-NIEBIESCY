/*
 * MaintenanceServiceTest.java
 *
 * Version: 1.0
 * Date: 2026-05-29
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MaintenanceService}.
 *
 * <p>Verifies the state management of the application's maintenance mode, including:
 * <ul>
 * <li>Default initial states (inactive).</li>
 * <li>Activation with proper cooldown time scheduling.</li>
 * <li>Immediate deactivation.</li>
 * <li>Force-activation logic.</li>
 * <li>Time-based state transitions (fully active verification).</li>
 * </ul>
 *
 * @version 1.0
 * @author EduLink Team
 */
class MaintenanceServiceTest {

    /**
     * The service instance under test.
     */
    private MaintenanceService maintenanceService;

    /**
     * Initializes a fresh, independent instance of the service
     * before each test execution to ensure test isolation.
     */
    @BeforeEach
    void setUp() {
        maintenanceService = new MaintenanceService();
    }

    /**
     * Test suite focusing on the standard activation and deactivation flows.
     */
    @Nested
    @DisplayName("Activation and Deactivation Flows")
    class ActivationTests {

        /**
         * Verifies that a newly instantiated service correctly initializes
         * all maintenance flags to false and nullifies timestamps.
         */
        @Test
        @DisplayName("Initial state → Inactive with no scheduled time")
        void initialState_isInactive() {
            assertFalse(maintenanceService.isActive(), "Service should be inactive by default");
            assertNull(maintenanceService.getStartsAt(), "StartsAt should be null by default");
            assertFalse(maintenanceService.isFullyActive(), "Service cannot be fully active by default");
        }

        /**
         * Asserts that activating the service accurately toggles the active flag
         * and schedules the fully-active state 20 minutes into the future.
         */
        @Test
        @DisplayName("activate() → Sets active flag and schedules 20 min cooldown")
        void activate_setsStateAndCooldown() {
            LocalDateTime beforeActivation = LocalDateTime.now();

            maintenanceService.activate();

            assertTrue(maintenanceService.isActive());
            assertNotNull(maintenanceService.getStartsAt());

            // StartsAt should be roughly 20 minutes from the activation moment
            LocalDateTime expectedStartsAt = beforeActivation.plusMinutes(20);

            // Verify it falls within a small acceptable margin of execution time (1 second)
            assertTrue(maintenanceService.getStartsAt().isAfter(expectedStartsAt.minusSeconds(1)));
            assertTrue(maintenanceService.getStartsAt().isBefore(expectedStartsAt.plusSeconds(1)));

            // Since 20 minutes haven't passed yet, it should not be fully active
            assertFalse(maintenanceService.isFullyActive());
        }

        /**
         * Confirms that deactivating the service instantly clears
         * all scheduling data and resets the active flag.
         */
        @Test
        @DisplayName("deactivate() → Resets all maintenance states immediately")
        void deactivate_resetsStates() {
            maintenanceService.activate();
            assertTrue(maintenanceService.isActive());

            maintenanceService.deactivate();

            assertFalse(maintenanceService.isActive());
            assertNull(maintenanceService.getStartsAt());
            assertFalse(maintenanceService.isFullyActive());
        }
    }

    /**
     * Test suite focusing on forced operations and time-dependent state resolutions.
     */
    @Nested
    @DisplayName("Force Operations and Time-based Logic")
    class ForceAndTimingTests {

        /**
         * Asserts that requesting a forced activation properly reduces
         * the existing cooldown to just 1 minute.
         */
        @Test
        @DisplayName("forceActivate() while active → Reduces cooldown to 1 minute")
        void forceActivate_whenActive_reducesCooldown() {
            maintenanceService.activate();
            LocalDateTime originalStartsAt = maintenanceService.getStartsAt();

            LocalDateTime beforeForce = LocalDateTime.now();
            maintenanceService.forceActivate();

            LocalDateTime newStartsAt = maintenanceService.getStartsAt();

            // The new start time should be strictly before the original 20-min schedule
            assertTrue(newStartsAt.isBefore(originalStartsAt));

            // The new start time should be roughly 1 minute from the force call
            LocalDateTime expectedNewStartsAt = beforeForce.plusMinutes(1);
            assertTrue(newStartsAt.isAfter(expectedNewStartsAt.minusSeconds(1)));
            assertTrue(newStartsAt.isBefore(expectedNewStartsAt.plusSeconds(1)));
        }

        /**
         * Confirms that attempting to force activate a service that isn't
         * currently scheduled for maintenance does absolutely nothing.
         */
        @Test
        @DisplayName("forceActivate() while inactive → Does nothing")
        void forceActivate_whenInactive_doesNothing() {
            maintenanceService.forceActivate();

            assertFalse(maintenanceService.isActive());
            assertNull(maintenanceService.getStartsAt());
        }

        /**
         * Uses Java Reflection to simulate the passage of time,
         * verifying that the system correctly reports as 'fully active'
         * once the cooldown period has completely elapsed.
         *
         * @throws Exception if reflection fails to access the private field
         */
        @Test
        @DisplayName("isFullyActive() → Returns true when cooldown time has passed")
        void isFullyActive_whenTimeHasPassed_returnsTrue() throws Exception {
            maintenanceService.activate();

            // Using reflection to artificially move the scheduled 'startsAt' to the past
            // (e.g., simulating that the 20 minutes have already elapsed)
            Field startsAtField = MaintenanceService.class.getDeclaredField("startsAt");
            startsAtField.setAccessible(true);
            startsAtField.set(maintenanceService, LocalDateTime.now().minusMinutes(5));

            assertTrue(maintenanceService.isActive(), "Service should remain active");
            assertTrue(maintenanceService.isFullyActive(), "Service should be fully active after cooldown expires");
        }
    }
}