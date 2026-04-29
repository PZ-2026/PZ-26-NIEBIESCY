/*
 * BookingRepository.java
 *
 * Version: 1.0
 * Date: 2026-04-26
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
 * Provides a custom query to retrieve all bookings for a specific student.
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
}