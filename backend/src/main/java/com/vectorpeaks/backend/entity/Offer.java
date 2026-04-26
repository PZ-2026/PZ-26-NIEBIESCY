/*
 * Offer.java
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
import java.math.BigDecimal;

/**
 * Entity representing a tutoring offer made by a tutor.
 * Contains pricing, subject, type (online/offline), and references to availability slot and status.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Identifier of the tutor who created the offer. */
    @Column(name = "tutor_id")
    private Integer tutorId;

    /** Price per hour with two decimal places. */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    /** Reference to an availability slot (optional). */
    @Column(name = "availability_slot_id")
    private Integer availabilitySlotId;

    /** Detailed description of the offer. */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** Identifier of the subject being taught. */
    @Column(name = "subject_id")
    private Integer subjectId;

    /** Identifier of the offer status (e.g., active, inactive). */
    @Column(name = "status_id")
    private Integer statusId;

    /** Identifier of the global limit rule (if any). */
    @Column(name = "global_limit_id")
    private Integer globalLimitId;

    /** Type of offer: "Online" or "Offline". */
    @Column(name = "offer_type")
    private String offerType;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTutorId() { return tutorId; }
    public void setTutorId(Integer tutorId) { this.tutorId = tutorId; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getAvailabilitySlotId() { return availabilitySlotId; }
    public void setAvailabilitySlotId(Integer availabilitySlotId) { this.availabilitySlotId = availabilitySlotId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public Integer getGlobalLimitId() { return globalLimitId; }
    public void setGlobalLimitId(Integer globalLimitId) { this.globalLimitId = globalLimitId; }

    public String getOfferType() { return offerType; }
    public void setOfferType(String offerType) { this.offerType = offerType; }
}