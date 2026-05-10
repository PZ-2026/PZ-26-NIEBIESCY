/*
 * AvailabilitySlotController.java
 *
 * Version: 1.0
 * Date: 2026-04-28
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */
package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.SlotDto;
import com.vectorpeaks.backend.entity.AvailabilitySlot;
import com.vectorpeaks.backend.repository.AvailabilitySlotRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing availability slots.
 * Provides an endpoint to retrieve all slots (global, defined by admin).
 *
 * @version 1.0
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/slots")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class AvailabilitySlotController {

    private final AvailabilitySlotRepository slotRepository;

    /**
     * Constructs a new AvailabilitySlotController with required repository.
     *
     * @param slotRepository repository for availability slots
     */
    public AvailabilitySlotController(AvailabilitySlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    /**
     * Retrieves all available slots as DTOs with human-readable labels.
     *
     * @return list of SlotDto objects
     */
    @GetMapping
    public List<SlotDto> getAllSlots() {
        return slotRepository.findAll().stream()
                .map(slot -> new SlotDto(slot.getId(), formatSlotLabel(slot)))
                .collect(Collectors.toList());
    }

    /**
     * Formats the slot as a human-readable string,
     * e.g., "Pon 10:00" .
     *
     * @param slot the slot entity
     * @return formatted label
     */
    private String formatSlotLabel(AvailabilitySlot slot) {
        String dayName = getDayName(slot.getDayOfWeek());
        String start = slot.getStartTime().toString().substring(0, 5);
        return dayName + " " + start;
    }

    /**
     * Converts numeric day-of-week to Polish short name.
     *
     * @param dayOfWeek day number (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
     * @return abbreviated day name
     */
    private String getDayName(Short dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return "Pon";
            case 2: return "Wt";
            case 3: return "Śr";
            case 4: return "Czw";
            case 5: return "Pt";
            case 6: return "Sob";
            case 0: return "Nd";
            default: return "";
        }
    }
}
