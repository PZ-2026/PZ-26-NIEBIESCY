/*
 * ReviewController.java
 *
 * Version: 1.2
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.ReviewRequest;
import com.vectorpeaks.backend.dto.ReviewResponse;
import com.vectorpeaks.backend.entity.Booking;
import com.vectorpeaks.backend.entity.Offer;
import com.vectorpeaks.backend.entity.Review;
import com.vectorpeaks.backend.entity.Subject;
import com.vectorpeaks.backend.repository.BookingRepository;
import com.vectorpeaks.backend.repository.OfferRepository;
import com.vectorpeaks.backend.repository.ReviewRepository;
import com.vectorpeaks.backend.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing reviews.
 * Provides endpoints to add/update reviews and retrieve reviews for a tutor.
 * Includes built-in BOLA (Broken Object Level Authorization) protection and security event logging.
 *
 * @version 1.1
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final OfferRepository offerRepository;
    private final SubjectRepository subjectRepository;

    /**
     * Constructs a new ReviewController with required repositories.
     *
     * @param reviewRepository repository for reviews
     * @param bookingRepository repository for bookings
     * @param offerRepository repository for offers
     * @param subjectRepository repository for subjects
     */
    public ReviewController(ReviewRepository reviewRepository,
                            BookingRepository bookingRepository,
                            OfferRepository offerRepository,
                            SubjectRepository subjectRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.offerRepository = offerRepository;
        this.subjectRepository = subjectRepository;
    }

    /**
     * Adds a new review or updates an existing one for a given booking.
     * If a review for the booking ID already exists, its rating and comment are updated.
     *
     * @param request the review request containing booking ID, tutor ID, rating, and comment
     * @param authentication the security context containing the logged-in user's details
     * @return ResponseEntity with success status
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<?> addOrUpdateReview(@RequestBody ReviewRequest request,
                                               org.springframework.security.core.Authentication authentication) {

        Optional<Booking> bookingOpt = bookingRepository.findById(request.getBookingId());
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Booking not found for the given ID.");
        }
        Booking booking = bookingOpt.get();

        Integer loggedInUserId = (Integer) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !booking.getStudentId().equals(loggedInUserId)) {
            logger.warn("SECURITY ALERT (BOLA): User ID {} attempted to modify review for Booking ID {} belonging to Student ID {}",
                    loggedInUserId, request.getBookingId(), booking.getStudentId());
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body("You can only review your own bookings.");
        }

        Optional<Review> existing = reviewRepository.findByBookingId(request.getBookingId());
        Review review;
        if (existing.isPresent()) {
            review = existing.get();
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setUpdatedAt(LocalDateTime.now());
        } else {
            review = new Review();
            review.setBookingId(request.getBookingId());
            review.setTutorId(request.getTutorId());
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setCreatedAt(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());
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
    @PreAuthorize("isAuthenticated()")
    public List<ReviewResponse> getReviewsByTutor(@PathVariable Integer tutorId) {
        return reviewRepository.findByTutorId(tutorId).stream()
                .map(review -> {
                    String date = "";
                    String subjectName = null;

                    Optional<Booking> bookingOpt =
                            bookingRepository.findById(review.getBookingId().intValue());
                    if (bookingOpt.isPresent()) {
                        Booking booking = bookingOpt.get();
                        date = review.getCreatedAt() != null
                                ? review.getCreatedAt().toLocalDate().toString()
                                : "";

                        Optional<Offer> offerOpt =
                                offerRepository.findById(booking.getOfferId());
                        if (offerOpt.isPresent()) {
                            Optional<Subject> subjectOpt =
                                    subjectRepository.findById(offerOpt.get().getSubjectId());
                            subjectName = subjectOpt.map(Subject::getName).orElse(null);
                        }
                    }

                    return new ReviewResponse(
                            review.getId(),
                            review.getRating(),
                            review.getComment(),
                            date,
                            subjectName
                    );
                })
                .collect(Collectors.toList());
    }
}