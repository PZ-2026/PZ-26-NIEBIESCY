/*
 * ReviewRepository.java
 *
 * Version: 1.1
 * Date: 2026-05-15
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on {@link Review} entities.
 *
 * @version 1.1
 * @author EduLink Team
 */
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tutorId = :tutorId")
    Double getAverageRatingByTutorId(@Param("tutorId") Integer tutorId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.tutorId = :tutorId")
    Integer countReviewsByTutorId(@Param("tutorId") Integer tutorId);

    Optional<Review> findByBookingId(Long bookingId);
    
    List<Review> findByTutorId(Integer tutorId);
}