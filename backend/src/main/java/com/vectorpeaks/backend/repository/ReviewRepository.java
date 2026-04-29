/*
 * ReviewRepository.java
 *
 * Version: 1.0
 * Date: 2026-04-26
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

import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on {@link Review} entities.
 * Provides custom queries to calculate average rating and review count per tutor,
 * and to find a review by booking ID.
 *
 * @version 1.0
 * @author EduLink Team
 */
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    /**
     * Retrieves the average rating for a given tutor.
     *
     * @param tutorId the ID of the tutor
     * @return the average rating as a Double, or null if no reviews exist
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tutorId = :tutorId")
    Double getAverageRatingByTutorId(@Param("tutorId") Integer tutorId);

    /**
     * Counts the number of reviews for a given tutor.
     *
     * @param tutorId the ID of the tutor
     * @return the total number of reviews
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.tutorId = :tutorId")
    Integer countReviewsByTutorId(@Param("tutorId") Integer tutorId);

    /**
     * Finds a review by the associated booking ID.
     *
     * @param bookingId the ID of the booking
     * @return an Optional containing the review if found, or empty otherwise
     */
    Optional<Review> findByBookingId(Long bookingId);
}