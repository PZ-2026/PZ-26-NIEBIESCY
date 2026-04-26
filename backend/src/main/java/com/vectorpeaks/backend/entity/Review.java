/*
 * Review.java
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

/**
 * Entity representing a review given by a student to a tutor.
 * Contains rating, optional comment, and reference to the related booking.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Rating value (e.g., 1-5 stars). */
    private Short rating;

    /** Identifier of the tutor being reviewed. */
    @Column(name = "tutor_id")
    private Integer tutorId;

    /** Optional textual comment from the student. */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /** Identifier of the booking associated with this review. */
    @Column(name = "booking_id")
    private Long bookingId;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Short getRating() { return rating; }
    public void setRating(Short rating) { this.rating = rating; }

    public Integer getTutorId() { return tutorId; }
    public void setTutorId(Integer tutorId) { this.tutorId = tutorId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
}