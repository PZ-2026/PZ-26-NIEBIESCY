/*
 * ReviewController.java
 *
 * Version: 1.0
 * Date: 2026-04-26
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.ReviewRequest;
import com.vectorpeaks.backend.entity.Review;
import com.vectorpeaks.backend.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for managing reviews.
 * Provides an endpoint to add or update a review for a booking.
 *
 * @version 1.0
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class ReviewController {

    private final ReviewRepository reviewRepository;

    /**
     * Constructs a new ReviewController with the required repository.
     *
     * @param reviewRepository repository for reviews
     */
    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * Adds a new review or updates an existing one for a given booking.
     * If a review for the booking ID already exists, its rating and comment are updated.
     *
     * @param request the review request containing booking ID, tutor ID, rating, and comment
     * @return ResponseEntity with success status
     */
    @PostMapping
    public ResponseEntity<?> addOrUpdateReview(@RequestBody ReviewRequest request) {
        Optional<Review> existing = reviewRepository.findByBookingId(request.getBookingId());
        Review review;
        if (existing.isPresent()) {
            review = existing.get();
            review.setRating(request.getRating());
            review.setComment(request.getComment());
        } else {
            review = new Review();
            review.setBookingId(request.getBookingId());
            review.setTutorId(request.getTutorId());
            review.setRating(request.getRating());
            review.setComment(request.getComment());
        }
        reviewRepository.save(review);
        return ResponseEntity.ok().build();
    }
}