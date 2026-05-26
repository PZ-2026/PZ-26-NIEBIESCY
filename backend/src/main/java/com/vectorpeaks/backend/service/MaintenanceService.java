/*
 * MaintenanceService.java
 *
 * Version: 1.1
 * Date: 2026-05-26
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service managing the maintenance mode state.
 * Stores state in memory (lost on restart – suitable for single-instance deployments).
 *
 * <p>When activated, a 20-minute countdown begins. During this period, non-admin clients
 * should display a warning banner. After the countdown expires, maintenance is considered
 * fully active.</p>
 *
 * @version 1.1
 * @author EduLink Team
 */
@Service
public class MaintenanceService {

    /** Whether maintenance mode has been scheduled or is active. */
    private boolean active = false;

    /** The time when maintenance mode becomes fully effective (after the cooldown). */
    private LocalDateTime startsAt = null;

    /** Cooldown duration in minutes before maintenance takes full effect. */
    private static final int COOLDOWN_MINUTES = 20;

    /**
     * Activates maintenance mode with a 20-minute cooldown.
     */
    public synchronized void activate() {
        this.active = true;
        this.startsAt = LocalDateTime.now().plusMinutes(COOLDOWN_MINUTES);
    }

    /**
     * Deactivates maintenance mode immediately.
     */
    public synchronized void deactivate() {
        this.active = false;
        this.startsAt = null;
    }

    /**
     * Returns whether maintenance mode is scheduled or active.
     *
     * @return true if maintenance is pending or active
     */
    public synchronized boolean isActive() {
        return active;
    }

    /**
     * Returns the time when full maintenance begins.
     *
     * @return the maintenance start time, or null if not active
     */
    public synchronized LocalDateTime getStartsAt() {
        return startsAt;
    }

    /**
     * Returns whether the cooldown has elapsed and maintenance is fully in effect.
     *
     * @return true if maintenance is active and the cooldown has passed
     */
    public synchronized boolean isFullyActive() {
        return active && startsAt != null && LocalDateTime.now().isAfter(startsAt);
    }

    /**
     * Shortens the maintenance cooldown to 1 minute from now.
     * Only works if maintenance has already been scheduled.
     */
    public synchronized void forceActivate() {
        if (active) {
            this.startsAt = LocalDateTime.now().plusMinutes(1);
        }
    }
}
