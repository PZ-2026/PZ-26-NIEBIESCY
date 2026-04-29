/*
 * Booking.java
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
import java.time.LocalDateTime;

/**
 * Entity representing a booking (lesson reservation) made by a student for a specific offer.
 * Stores references to the offer, student, availability slot, booking status, and date.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Identifier of the selected availability slot. */
    @Column(name = "availability_slot_id")
    private Integer availabilitySlotId;

    /** Identifier of the booking status (e.g., 3 = PENDING, 6 = ACCEPTED, etc.). */
    @Column(name = "status_id")
    private Integer statusId;

    /** Identifier of the offer being booked. */
    @Column(name = "offer_id")
    private Integer offerId;

    /** Identifier of the student making the booking. */
    @Column(name = "student_id")
    private Integer studentId;

    /** Date and time when the booking was created. */
    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAvailabilitySlotId() { return availabilitySlotId; }
    public void setAvailabilitySlotId(Integer availabilitySlotId) { this.availabilitySlotId = availabilitySlotId; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public Integer getOfferId() { return offerId; }
    public void setOfferId(Integer offerId) { this.offerId = offerId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
}