/*
 * OfferCreateRequest.java
 *
 * Version: 1.1
 * Date: 2026-05-22
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object (DTO) for creating a new tutoring offer.
 * Contains all information needed to create an offer by a tutor.
 *
 * @version 1.1
 * @author EduLink Team
 */

public class OfferCreateRequest {

    /** Identifier of the tutor creating the offer. */
    private Integer tutorId;

    /** Identifier of the subject being taught. */
    private Integer subjectId;

    /** Detailed description of the offer. */
    private String details;

    /** Price per hour in the local currency. */
    private BigDecimal price;

    /** Type of offer: "Online" or "Offline". */
    private String offerType;

    /** Identifier of the chosen availability slot (optional, may be null). */
    private List<Integer> availabilitySlotIds;

    // Getters and setters

    public Integer getTutorId() { return tutorId; }
    public void setTutorId(Integer tutorId) { this.tutorId = tutorId; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getOfferType() { return offerType; }
    public void setOfferType(String offerType) { this.offerType = offerType; }

    public List<Integer> getAvailabilitySlotIds() { return availabilitySlotIds; }
    public void setAvailabilitySlotIds(List<Integer> availabilitySlotIds) { this.availabilitySlotIds = availabilitySlotIds; }
}
