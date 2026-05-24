/*
 * BookingRepository.java
 *
 * Version: 1.1
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for performing CRUD operations on {@link Booking} entities.
 * Provides custom queries to retrieve bookings by student or by offer and slot.
 *
 * @version 1.0
 * @author EduLink Team
 */
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    /**
     * Retrieves all bookings made by a given student.
     *
     * @param studentId the ID of the student
     * @return list of bookings belonging to the student
     */
    List<Booking> findByStudentId(Integer studentId);

    /**
     * Retrieves all bookings for a specific offer and availability slot.
     *
     * @param offerId the ID of the offer
     * @param availabilitySlotId the ID of the availability slot
     * @return list of bookings matching the given offer and slot
     */
    List<Booking> findByOfferIdAndAvailabilitySlotId(Integer offerId, Integer availabilitySlotId);
}