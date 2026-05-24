/*
 * SlotDto.java
 *
 * Version: 1.2
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.dto;

/**
 * Data Transfer Object (DTO) for an availability slot.
 * Contains the slot ID and a human-readable label (e.g., "Pon 10:00").
 *
 * @version 1.1
 * @author EduLink Team
 */
public class SlotDto {
    /** Unique identifier of the availability slot. */
    private Integer id;

    /** Human-readable label combining day and start time. */
    private String label;

    /** Numeric representation of the day of week (e.g., 1 = Monday, 7 = Sunday). */
    private Integer dayOfWeek;

    /** Indicates whether the slot is already booked. */
    private boolean isBooked;

    /**
     * Constructs a new SlotDto with the given ID, label, day of week, and booked status.
     *
     * @param id        the slot identifier
     * @param label     the display label
     * @param dayOfWeek the day of week as an integer (1-7)
     * @param isBooked  true if the slot is already booked
     */
    public SlotDto(Integer id, String label, Integer dayOfWeek, boolean isBooked) {
        this.id = id;
        this.label = label;
        this.dayOfWeek = dayOfWeek;
        this.isBooked = isBooked;
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }
}