/*
 * AvailabilitySlot.java
 *
 * Version: 1.0
 * Date: 2026-04-26
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

/**
 * Entity representing a time slot when a tutor is available.
 * Stores day of week and start/end times.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Entity
@Table(name = "availability_slots")
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Day of week: 0 = Sunday, 1 = Monday, ..., 6 = Saturday. */
    @Column(name = "day_of_week")
    private Short dayOfWeek;

    /** Start time of the availability slot. */
    @Column(name = "start_time")
    private LocalTime startTime;

    /** End time of the availability slot. */
    @Column(name = "end_time")
    private LocalTime endTime;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Short getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Short dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}