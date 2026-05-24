/*
 * ReviewResponse.java
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
 * Data Transfer Object for returning review data to the frontend.
 * Contains rating, optional comment, the booking date as the review date,
 * and the subject name.
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

    /** Name of the subject for which the booking was made. */
    private String subjectName;

    /**
     * Constructs a new ReviewResponse with all fields.
     *
     * @param id          the review ID
     * @param rating      the rating value
     * @param comment     the review comment
     * @param date        the booking date
     * @param subjectName the subject name
     */
    public ReviewResponse(Integer id, Short rating, String comment, String date, String subjectName) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
        this.subjectName = subjectName;
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Short getRating() { return rating; }
    public void setRating(Short rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
}