/*
 * ReviewRequest.java
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
 * Data Transfer Object (DTO) for submitting or updating a review.
 * Contains the booking ID, tutor ID, rating, and optional comment.
 *
 * @version 1.0
 * @author EduLink Team
 */
public class ReviewRequest {
    /** Identifier of the booking being reviewed. */
    private Long bookingId;

    /** Identifier of the tutor receiving the review. */
    private Integer tutorId;

    /** Rating value (typically 1-5). */
    private Short rating;

    /** Optional text comment. */
    private String comment;

    // Getters and setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Integer getTutorId() { return tutorId; }
    public void setTutorId(Integer tutorId) { this.tutorId = tutorId; }

    public Short getRating() { return rating; }
    public void setRating(Short rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}