/*
 * SlotDto.java
 *
 * Version: 1.0
 * Date: 2026-04-26
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
 * @version 1.0
 * @author EduLink Team
 */
public class SlotDto {
    /** Unique identifier of the availability slot. */
    private Integer id;

    /** Human-readable label combining day and start time. */
    private String label;

    /**
     * Constructs a new SlotDto with the given ID and label.
     *
     * @param id    the slot identifier
     * @param label the display label
     */
    public SlotDto(Integer id, String label) {
        this.id = id;
        this.label = label;
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}