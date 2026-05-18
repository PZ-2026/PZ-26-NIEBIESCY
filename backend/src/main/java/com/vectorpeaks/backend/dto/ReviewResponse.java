/*
 * ReviewResponse.java
 *
 * Version: 1.0
 * Date: 2026-05-15
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.dto;

/**
 * Data Transfer Object for returning review data to the frontend.
 * Contains rating, optional comment and the booking date as the review date.
 *
 * @version 1.0
 * @author EduLink Team
 */
public class ReviewResponse {

    /** Unique identifier of the review. */
    private Integer id;

    /** Rating value (1–5). */
    private Short rating;

    /** Optional text comment left by the student. */
    private String comment;

    /** Date of the associated booking (ISO format: yyyy-MM-dd). */
    private String date;

    public ReviewResponse(Integer id, Short rating, String comment, String date) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Short getRating() { return rating; }
    public void setRating(Short rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}