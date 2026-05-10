///*
// * ReviewControllerTest.java
// *
// * Version: 1.0
// * Date: 2026-05-03
// *
// * Copyright (c) 2026 EduLink Team. All rights reserved.
// *
// * This software is the confidential and proprietary information of EduLink.
// */
//
//package com.vectorpeaks.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.vectorpeaks.backend.dto.ReviewRequest;
//import com.vectorpeaks.backend.entity.Review;
//import com.vectorpeaks.backend.repository.ReviewRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
///**
// * Unit tests for {@link ReviewController}.
// *
// * <p>Verifies the behaviour of the {@code POST /api/reviews} endpoint, including:
// * <ul>
// *   <li>creating a new review when none exists for the given booking,</li>
// *   <li>updating an existing review when one already exists for the booking.</li>
// * </ul>
// *
// * <p>Uses {@code @WebMvcTest} with {@link MockMvc} – only the controller layer
// * is loaded; no full Spring context or database is required.
// * The {@link ReviewRepository} dependency is replaced by a Mockito mock
// * ({@code @MockitoBean}).
// *
// * @version 1.0
// * @author EduLink Team
// * @see ReviewController
// */
//@WebMvcTest(ReviewController.class)
//class ReviewControllerTest {
//
//    /** HTTP client used to perform requests in web-layer tests. */
//    @Autowired
//    private MockMvc mockMvc;
//
//    /** Mock of the review repository – replaces the database layer. */
//    @MockitoBean
//    private ReviewRepository reviewRepository;
//
//    /** JSON mapper used to serialize request objects. */
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // -----------------------------------------------------------------------
//    // POST /api/reviews – new review
//    // -----------------------------------------------------------------------
//
//    /**
//     * Verifies that the endpoint returns {@code 200 OK} and saves a new
//     * {@link Review} when no review exists for the given booking ID.
//     *
//     * @throws Exception if the MockMvc request execution fails
//     */
//    @Test
//    void addOrUpdateReview_noExistingReview_createsNewAndReturns200() throws Exception {
//        when(reviewRepository.findByBookingId(1L)).thenReturn(Optional.empty());
//        when(reviewRepository.save(any(Review.class))).thenReturn(new Review());
//
//        ReviewRequest request = buildReviewRequest(1L, 5, (short) 5, "Great tutor!");
//
//        mockMvc.perform(post("/api/reviews")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//
//        verify(reviewRepository, times(1)).save(any(Review.class));
//    }
//
//    // -----------------------------------------------------------------------
//    // POST /api/reviews – update existing review
//    // -----------------------------------------------------------------------
//
//    /**
//     * Verifies that the endpoint returns {@code 200 OK} and updates the
//     * existing {@link Review} (rating and comment) when a review for the
//     * given booking already exists.
//     *
//     * @throws Exception if the MockMvc request execution fails
//     */
//    @Test
//    void addOrUpdateReview_existingReview_updatesAndReturns200() throws Exception {
//        Review existing = buildReview(10L, 1L, 5, (short) 3, "Good");
//        when(reviewRepository.findByBookingId(1L)).thenReturn(Optional.of(existing));
//        when(reviewRepository.save(any(Review.class))).thenReturn(existing);
//
//        ReviewRequest request = buildReviewRequest(1L, 5, (short) 5, "Excellent!");
//
//        mockMvc.perform(post("/api/reviews")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//
//        verify(reviewRepository, times(1)).save(any(Review.class));
//    }
//
//    /**
//     * Verifies that when updating a review the repository's {@code save}
//     * is called exactly once – the old review is reused, not duplicated.
//     *
//     * @throws Exception if the MockMvc request execution fails
//     */
//    @Test
//    void addOrUpdateReview_existingReview_savesExactlyOnce() throws Exception {
//        Review existing = buildReview(10L, 2L, 5, (short) 2, "OK");
//        when(reviewRepository.findByBookingId(2L)).thenReturn(Optional.of(existing));
//        when(reviewRepository.save(any(Review.class))).thenReturn(existing);
//
//        ReviewRequest request = buildReviewRequest(2L, 5, (short) 4, "Better now");
//
//        mockMvc.perform(post("/api/reviews")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//
//        verify(reviewRepository, times(1)).save(existing);
//    }
//
//    // -----------------------------------------------------------------------
//    // Helpers
//    // -----------------------------------------------------------------------
//
//    /**
//     * Creates a {@link ReviewRequest} with the given data.
//     *
//     * @param bookingId booking identifier
//     * @param tutorId   tutor identifier
//     * @param rating    review rating
//     * @param comment   review comment
//     * @return populated {@link ReviewRequest}
//     */
//    private ReviewRequest buildReviewRequest(Long bookingId, Integer tutorId,
//                                             Short rating, String comment) {
//        ReviewRequest r = new ReviewRequest();
//        r.setBookingId(bookingId);
//        r.setTutorId(tutorId);
//        r.setRating(rating);
//        r.setComment(comment);
//        return r;
//    }
//
//    /**
//     * Creates a {@link Review} entity with the given data.
//     *
//     * @param id        review identifier
//     * @param bookingId booking identifier
//     * @param tutorId   tutor identifier
//     * @param rating    review rating
//     * @param comment   review comment
//     * @return populated {@link Review}
//     */
//    private Review buildReview(Long id, Long bookingId, Integer tutorId,
//                               Short rating, String comment) {
//        Review r = new Review();
//        r.setId(id);
//        r.setBookingId(bookingId);
//        r.setTutorId(tutorId);
//        r.setRating(rating);
//        r.setComment(comment);
//        return r;
//    }
//}