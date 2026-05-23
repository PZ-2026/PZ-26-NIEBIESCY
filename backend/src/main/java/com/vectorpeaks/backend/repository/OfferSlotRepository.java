/*
 * OfferSlotRepository.java
 *
 * Version: 1.0
 * Date: 2026-05-23
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.OfferSlot;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for OfferSlot entities.
 * Provides CRUD operations and custom queries for managing the join table
 * between offers and availability slots.
 */

public interface OfferSlotRepository extends JpaRepository<OfferSlot, Integer> {

    /**
     * Retrieves all offer-slot associations for a specific offer.
     * @param offerId the ID of the offer
     * @return a list of OfferSlot records belonging to the given offer
     */

    List<OfferSlot> findByOfferId(Integer offerId);

    /**
     * Deletes all offer-slot associations for a specific offer.
     * @param offerId the ID of the offer whose slots should be deleted
     */

    @Modifying
    @Transactional
    @Query("DELETE FROM OfferSlot os WHERE os.offerId = :offerId")
    void deleteByOfferId(@Param("offerId") Integer offerId);

    /**
     * Retrieves all offer-slot associations for a specific availability slot.
     * @param availabilitySlotId the ID of the availability slot
     * @return a list of OfferSlot records linked to the specified slot
     */

    List<OfferSlot> findByAvailabilitySlotId(Integer availabilitySlotId);
}