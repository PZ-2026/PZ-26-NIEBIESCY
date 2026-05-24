/*
 * ReviewRequest.java
 *
 * Version: 1.1
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.dto;

/**
 * Data Transfer Object (DTO) for submitting or updating a review.
 * Contains the booking ID, tutor ID, rating, and optional comment.
 *
 * @version 1.0
 * @author EduLink Team
 */
public class ReviewRequest {
    /** Identifier of the booking being reviewed. */
    private Integer bookingId;

    /** Identifier of the tutor receiving the review. */
    private Integer tutorId;

    /** Rating value (typically 1-5). */
    private Short rating;

    /** Optional text comment. */
    private String comment;

    // Getters and setters
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getTutorId() { return tutorId; }
    public void setTutorId(Integer tutorId) { this.tutorId = tutorId; }

    public Short getRating() { return rating; }
    public void setRating(Short rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}