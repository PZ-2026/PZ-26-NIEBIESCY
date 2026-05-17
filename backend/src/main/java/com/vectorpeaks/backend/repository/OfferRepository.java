/*
 * OfferRepository.java
 *
 * Version: 1.0
 * Date: 2026-04-26
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Integer> {

    /**
     * Finds all offers with a given status ID.
     *
     * @param statusId the status identifier to filter by
     * @return list of offers matching the given status
     */
    List<Offer> findByStatusId(Integer statusId);
}