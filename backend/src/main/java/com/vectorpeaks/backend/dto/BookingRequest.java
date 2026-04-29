/*
 * BookingRequest.java
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
 * Data Transfer Object (DTO) for creating a new booking.
 * Contains the identifiers of the offer, student, and selected availability slot.
 *
 * @version 1.0
 * @author EduLink Team
 */
public class BookingRequest {
    /** Identifier of the offer being booked. */
    private Integer offerId;

    /** Identifier of the student making the booking. */
    private Integer studentId;

    /** Identifier of the chosen availability slot. */
    private Integer availabilitySlotId;

    // Getters and setters
    public Integer getOfferId() { return offerId; }
    public void setOfferId(Integer offerId) { this.offerId = offerId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getAvailabilitySlotId() { return availabilitySlotId; }
    public void setAvailabilitySlotId(Integer availabilitySlotId) { this.availabilitySlotId = availabilitySlotId; }
}