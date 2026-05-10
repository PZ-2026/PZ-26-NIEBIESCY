/*
 * BookingResponse.java
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
 * Data Transfer Object (DTO) for booking information returned to the client.
 * Contains all details needed to display a booking in the frontend.
 *
 * @version 1.0
 * @author EduLink Team
 */
public class BookingResponse {
    /** Unique identifier of the booking. */
    private Integer id;

    /** Identifier of the associated offer. */
    private Integer offerId;

    /** Name of the subject being taught. */
    private String subject;

    /** Full name of the tutor. */
    private String tutorName;

    /** Date of the booked lesson (YYYY-MM-DD). */
    private String date;

    /** Time of the booked lesson (HH:MM). */
    private String time;

    /** Price per hour of the lesson. */
    private Double price;

    /** Status of the booking (e.g., PENDING, ACCEPTED, REJECTED, COMPLETED). */
    private String status;

    /** Rating given by the student (1-5), or null if not yet rated. */
    private Short rating;

    /** Identifier of the tutor (used for submitting a review). */
    private Integer tutorId;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOfferId() { return offerId; }
    public void setOfferId(Integer offerId) { this.offerId = offerId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Short getRating() { return rating; }
    public void setRating(Short rating) { this.rating = rating; }

    public Integer getTutorId() { return tutorId; }
    public void setTutorId(Integer tutorId) { this.tutorId = tutorId; }
}