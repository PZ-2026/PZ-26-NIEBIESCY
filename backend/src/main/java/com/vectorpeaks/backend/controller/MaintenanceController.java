/*
 * MaintenanceController.java
 *
 * Version: 1.1
 * Date: 2026-05-26
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.service.MaintenanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for maintenance mode operations.
 * Provides endpoints for admin toggling and public status checking.
 *
 * @version 1.1
 * @author EduLink Team
 */
@RestController
@CrossOrigin(origins = "*") // For development only – restrict in production
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    /**
     * Constructs a new MaintenanceController.
     *
     * @param maintenanceService service managing maintenance state
     */
    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    /**
     * Returns the current maintenance mode status.
     * Public endpoint – accessible by all clients.
     *
     * @return JSON with active flag, startsAt timestamp, and fullyActive flag
     */
    @GetMapping("/api/maintenance/status")
    public Map<String, Object> getMaintenanceStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("active", maintenanceService.isActive());
        result.put("startsAt", maintenanceService.getStartsAt() != null
                ? maintenanceService.getStartsAt().toString() : null);
        result.put("fullyActive", maintenanceService.isFullyActive());
        return result;
    }

    /**
     * Toggles maintenance mode on or off (admin only).
     *
     * @param body map containing "active" boolean
     * @return 200 OK with updated status
     */
    @PutMapping("/api/admin/maintenance")
    public ResponseEntity<Map<String, Object>> toggleMaintenance(@RequestBody Map<String, Boolean> body) {
        Boolean active = body.get("active");
        Boolean force = body.get("force");

        if (Boolean.TRUE.equals(force)) {
            if (!maintenanceService.isActive()) {
                return ResponseEntity.badRequest().build();
            }
            maintenanceService.forceActivate();
        } else if (active != null) {
            if (active) {
                maintenanceService.activate();
            } else {
                maintenanceService.deactivate();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(getMaintenanceStatus());
    }

}
