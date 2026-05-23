/*
 * OfferSlot.java
 *
 * Version: 1.0
 * Date: 2026-05-23
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.entity;

import jakarta.persistence.*;

/**
 * Join entity linking an offer to its available time slots.
 * This table represents the many-to-many relationship between offers and availability slots.
 *
 * @version 1.0
 * @author EduLink Team
 */

@Entity
@Table(name = "offer_slots")
public class OfferSlot {

    /** Unique identifier of the join record (auto-generated). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Identifier of the offer to which this slot belongs. */
    @Column(name = "offer_id")
    private Integer offerId;

    /** Identifier of the availability slot (day + time) assigned to the offer. */
    @Column(name = "availability_slot_id")
    private Integer availabilitySlotId;

    // Getters and setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOfferId() { return offerId; }
    public void setOfferId(Integer offerId) { this.offerId = offerId; }

    public Integer getAvailabilitySlotId() { return availabilitySlotId; }
    public void setAvailabilitySlotId(Integer availabilitySlotId) { this.availabilitySlotId = availabilitySlotId; }
}
