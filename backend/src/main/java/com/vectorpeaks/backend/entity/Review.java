/*
 * Review.java
 *
 * Version: 1.1
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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
    private Integer bookingId;

    /** Timestamp when the review was created. */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the review was last updated. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Short getRating() { return rating; }
    public void setRating(Short rating) { this.rating = rating; }

    public Integer getTutorId() { return tutorId; }
    public void setTutorId(Integer tutorId) { this.tutorId = tutorId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}