/*
 * ReviewController.java
 *
 * Version: 1.1
 * Date: 2026-05-17
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.ReviewRequest;
import com.vectorpeaks.backend.dto.ReviewResponse;
import com.vectorpeaks.backend.entity.Booking;
import com.vectorpeaks.backend.entity.Review;
import com.vectorpeaks.backend.repository.BookingRepository;
import com.vectorpeaks.backend.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing reviews.
 * Provides endpoints to add/update reviews and retrieve reviews for a tutor.
 *
 * @version 1.1
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    /**
     * Constructs a new ReviewController with required repositories.
     *
     * @param reviewRepository repository for reviews
     * @param bookingRepository repository for bookings (used to fetch booking date)
     */
    public ReviewController(ReviewRepository reviewRepository,
                            BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
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

    /**
     * Retrieves all reviews for a given tutor, ordered by booking date descending.
     * Each review includes rating, comment and the booking date as the review date.
     *
     * @param tutorId the ID of the tutor
     * @return list of ReviewResponse DTOs
     */
    @GetMapping("/tutor/{tutorId}")
    public List<ReviewResponse> getReviewsByTutor(@PathVariable Integer tutorId) {
        return reviewRepository.findByTutorId(tutorId).stream()
                .map(review -> {
                    String date = "";
                    Optional<Booking> bookingOpt =
                            bookingRepository.findById(review.getBookingId().intValue());
                    if (bookingOpt.isPresent() && bookingOpt.get().getBookingDate() != null) {
                        date = bookingOpt.get().getBookingDate()
                                .toLocalDate().toString();
                    }
                    return new ReviewResponse(
                            review.getId(),
                            review.getRating(),
                            review.getComment(),
                            date
                    );
                })
                .collect(Collectors.toList());
    }
}