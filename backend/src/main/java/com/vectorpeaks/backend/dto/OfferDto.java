/*
 * OfferDto.java
 *
 * Version: 1.0
 * Date: 2026-04-26
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.dto;

import java.util.List;

/**
 * Data Transfer Object (DTO) for tutoring offers.
 * Contains all information needed to display an offer in the frontend,
 * including tutor details, subject, pricing, location, ratings, and availability.
 *
 * @version 1.0
 * @author EduLink Team
 */
public class OfferDto {
    /** Unique identifier of the offer. */
    private Integer id;

    /** Identifier of the tutor who created the offer. */
    private Integer tutorId;

    /** Full name of the tutor (first + last name). */
    private String tutorName;

    /** Name of the subject being taught. */
    private String subject;

    /** Detailed description of the offer. */
    private String description;

    /** Price per hour in the local currency. */
    private Double pricePerHour;

    /** City where the tutoring takes place (if offline). */
    private String city;

    /** Indicates whether tutoring is online only. */
    private Boolean isOnline;

    /** Average rating of the tutor (0.0 - 5.0). */
    private Float rating;

    /** Total number of reviews for this tutor. */
    private Integer reviewCount;

    /** List of available time slots (e.g., "Pon 10:00"). */
    private List<SlotDto> availableSlots;

    /** Approval status of the offer (default true). */
    private Boolean isApproved = true;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTutorId() { return tutorId; }
    public void setTutorId(Integer tutorId) { this.tutorId = tutorId; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }

    public Float getRating() { return rating; }
    public void setRating(Float rating) { this.rating = rating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public List<SlotDto> getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(List<SlotDto> availableSlots) { this.availableSlots = availableSlots; }

    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
}