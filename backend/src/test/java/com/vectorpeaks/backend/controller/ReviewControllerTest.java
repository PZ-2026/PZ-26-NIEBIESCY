/*
 * ReviewControllerTest.java
 *
 * Version: 2.0
 * Date: 2026-05-28
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorpeaks.backend.dto.ReviewRequest;
import com.vectorpeaks.backend.entity.Booking;
import com.vectorpeaks.backend.entity.Offer;
import com.vectorpeaks.backend.entity.Review;
import com.vectorpeaks.backend.entity.Subject;
import com.vectorpeaks.backend.repository.BookingRepository;
import com.vectorpeaks.backend.repository.OfferRepository;
import com.vectorpeaks.backend.repository.ReviewRepository;
import com.vectorpeaks.backend.repository.SubjectRepository;
import com.vectorpeaks.backend.service.MaintenanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ReviewController}.
 *
 * <p>Verifies the behaviour of the following endpoints:
 * <ul>
 *   <li>{@code POST /api/reviews} – add a new review or update an existing one,</li>
 *   <li>{@code GET /api/reviews/tutor/{tutorId}} – retrieve all reviews for a tutor.</li>
 * </ul>
 *
 * <p>Uses {@code @WebMvcTest} with {@link MockMvc} – only the controller layer
 * is loaded; no full Spring context or database is required.
 * All repository dependencies are replaced by Mockito mocks ({@code @MockitoBean}).
 *
 * <p>Security context is injected via
 * {@link org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors#authentication}
 * so that {@code @PreAuthorize} rules and BOLA checks inside the controller
 * are exercised as in production.
 *
 * @version 2.0
 * @author EduLink Team
 * @see ReviewController
 */
@WebMvcTest(
        controllers = ReviewController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
class ReviewControllerTest extends BaseControllerTest {

    /** HTTP client used to perform requests in web-layer tests. */
    @Autowired
    private MockMvc mockMvc;

    /** JSON mapper used to serialize request objects. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Mock of the review repository – replaces the database layer. */
    @MockitoBean
    private ReviewRepository reviewRepository;

    /** Mock of the booking repository – required for BOLA ownership checks. */
    @MockitoBean
    private BookingRepository bookingRepository;

    /** Mock of the offer repository – required for {@code getReviewsByTutor} enrichment. */
    @MockitoBean
    private OfferRepository offerRepository;

    /** Mock of the subject repository – required for {@code getReviewsByTutor} enrichment. */
    @MockitoBean
    private SubjectRepository subjectRepository;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Creates a {@link UsernamePasswordAuthenticationToken} suitable for
     * injecting into MockMvc requests via
     * {@link org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors#authentication}.
     *
     * <p>The principal is set to {@code userId} (an {@link Integer}), matching the
     * cast {@code (Integer) authentication.getPrincipal()} inside
     * {@link ReviewController#addOrUpdateReview}.
     *
     * @param userId   the authenticated user's ID (used as the principal)
     * @param roleName the Spring Security role name, e.g. {@code "ROLE_STUDENT"}
     * @return a fully populated authentication token
     */
    private UsernamePasswordAuthenticationToken getMockAuth(Integer userId, String roleName) {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority(roleName))
        );
    }

    /**
     * Creates a {@link ReviewRequest} populated with the provided values.
     *
     * @param bookingId booking identifier
     * @param tutorId   tutor identifier
     * @param rating    review rating (1–5)
     * @param comment   review comment text
     * @return populated {@link ReviewRequest}
     */
    private ReviewRequest buildReviewRequest(Integer bookingId, Integer tutorId,
                                             Short rating, String comment) {
        ReviewRequest r = new ReviewRequest();
        r.setBookingId(bookingId);
        r.setTutorId(tutorId);
        r.setRating(rating);
        r.setComment(comment);
        return r;
    }

    /**
     * Creates a {@link Review} entity populated with the provided values.
     *
     * @param id        review identifier
     * @param bookingId booking identifier
     * @param tutorId   tutor identifier
     * @param rating    review rating (1–5)
     * @param comment   review comment text
     * @return populated {@link Review}
     */
    private Review buildReview(Integer id, Integer bookingId, Integer tutorId,
                               Short rating, String comment) {
        Review r = new Review();
        r.setId(id);
        r.setBookingId(bookingId);
        r.setTutorId(tutorId);
        r.setRating(rating);
        r.setComment(comment);
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        return r;
    }

    /**
     * Creates a {@link Booking} entity owned by the given student and linked
     * to the given offer.
     *
     * @param id        booking identifier
     * @param studentId owning student's identifier
     * @param offerId   associated offer identifier
     * @return populated {@link Booking}
     */
    private Booking buildBooking(Integer id, Integer studentId, Integer offerId) {
        Booking b = new Booking();
        b.setId(id);
        b.setStudentId(studentId);
        b.setOfferId(offerId);
        b.setStatusId(4); // COMPLETED
        b.setBookingDate(LocalDateTime.of(2026, 5, 1, 10, 0));
        return b;
    }

    // -----------------------------------------------------------------------
    // POST /api/reviews – create new review
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 200 OK} and persists a new
     * {@link Review} when no review exists for the given booking and the
     * authenticated student is the booking owner.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void addOrUpdateReview_noExistingReview_createsNewAndReturns200() throws Exception {
        Booking booking = buildBooking(1, 10, 1);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(reviewRepository.findByBookingId(1)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(new Review());

        ReviewRequest request = buildReviewRequest(1, 5, (short) 5, "Great tutor!");

        mockMvc.perform(post("/api/reviews")
                        .with(authentication(getMockAuth(10, "ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    // -----------------------------------------------------------------------
    // POST /api/reviews – update existing review
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 200 OK} and updates the
     * existing {@link Review} when one already exists for the given booking
     * and the authenticated student is the booking owner.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void addOrUpdateReview_existingReview_updatesAndReturns200() throws Exception {
        Booking booking = buildBooking(1, 10, 1);
        Review existing = buildReview(10, 1, 5, (short) 3, "Good");

        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(reviewRepository.findByBookingId(1)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenReturn(existing);

        ReviewRequest request = buildReviewRequest(1, 5, (short) 5, "Excellent!");

        mockMvc.perform(post("/api/reviews")
                        .with(authentication(getMockAuth(10, "ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    /**
     * Verifies that when updating a review the repository's {@code save}
     * is called with the same existing entity – the old review is reused,
     * not duplicated.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void addOrUpdateReview_existingReview_savesExactlyOnce() throws Exception {
        Booking booking = buildBooking(2, 10, 1);
        Review existing = buildReview(10, 2, 5, (short) 2, "OK");

        when(bookingRepository.findById(2)).thenReturn(Optional.of(booking));
        when(reviewRepository.findByBookingId(2)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenReturn(existing);

        ReviewRequest request = buildReviewRequest(2, 5, (short) 4, "Better now");

        mockMvc.perform(post("/api/reviews")
                        .with(authentication(getMockAuth(10, "ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(reviewRepository, times(1)).save(existing);
    }

    // -----------------------------------------------------------------------
    // POST /api/reviews – BOLA protection
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 403 Forbidden} when the
     * authenticated user is not the owner of the booking (BOLA attack attempt).
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void addOrUpdateReview_notBookingOwner_returns403Forbidden() throws Exception {
        // Booking belongs to student 10, but user 999 tries to post a review
        Booking booking = buildBooking(1, 10, 1);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        ReviewRequest request = buildReviewRequest(1, 5, (short) 5, "Attempt");

        mockMvc.perform(post("/api/reviews")
                        .with(authentication(getMockAuth(999, "ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only review your own bookings."));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    /**
     * Verifies that an admin user can post a review for any booking,
     * regardless of ownership – BOLA check is bypassed for admins.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void addOrUpdateReview_adminUser_bypassesOwnershipCheckAndReturns200() throws Exception {
        // Booking belongs to student 10; admin (ID 1) posts a review
        Booking booking = buildBooking(1, 10, 1);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(reviewRepository.findByBookingId(1)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(new Review());

        ReviewRequest request = buildReviewRequest(1, 5, (short) 5, "Admin review");

        mockMvc.perform(post("/api/reviews")
                        .with(authentication(getMockAuth(1, "ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    // -----------------------------------------------------------------------
    // POST /api/reviews – booking not found
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 400 Bad Request} when no
     * booking exists for the given booking ID in the request body.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void addOrUpdateReview_bookingNotFound_returns400() throws Exception {
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        ReviewRequest request = buildReviewRequest(999, 5, (short) 4, "No booking");

        mockMvc.perform(post("/api/reviews")
                        .with(authentication(getMockAuth(10, "ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Booking not found for the given ID."));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    // -----------------------------------------------------------------------
    // GET /api/reviews/tutor/{tutorId}
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 200 OK} and a JSON array
     * containing all reviews for the given tutor, with subject names resolved.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getReviewsByTutor_reviewsExist_returns200AndList() throws Exception {
        Integer tutorId = 5;

        Review r1 = buildReview(1, 100, tutorId, (short) 5, "Excellent!");
        Review r2 = buildReview(2, 101, tutorId, (short) 4, "Very good");

        Booking b1 = buildBooking(100, 10, 1);
        Booking b2 = buildBooking(101, 11, 2);

        Offer offer1 = new Offer();
        offer1.setId(1);
        offer1.setTutorId(tutorId);
        offer1.setSubjectId(1);
        offer1.setPrice(BigDecimal.valueOf(80));

        Offer offer2 = new Offer();
        offer2.setId(2);
        offer2.setTutorId(tutorId);
        offer2.setSubjectId(2);
        offer2.setPrice(BigDecimal.valueOf(90));

        Subject math = new Subject();
        math.setId(1);
        math.setName("Mathematics");

        Subject physics = new Subject();
        physics.setId(2);
        physics.setName("Physics");

        when(reviewRepository.findByTutorId(tutorId)).thenReturn(List.of(r1, r2));
        when(bookingRepository.findById(100)).thenReturn(Optional.of(b1));
        when(bookingRepository.findById(101)).thenReturn(Optional.of(b2));
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer1));
        when(offerRepository.findById(2)).thenReturn(Optional.of(offer2));
        when(subjectRepository.findById(1)).thenReturn(Optional.of(math));
        when(subjectRepository.findById(2)).thenReturn(Optional.of(physics));

        mockMvc.perform(get("/api/reviews/tutor/{tutorId}", tutorId)
                        .with(authentication(getMockAuth(10, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].comment").value("Excellent!"))
                .andExpect(jsonPath("$[0].subjectName").value("Mathematics"))
                .andExpect(jsonPath("$[1].rating").value(4))
                .andExpect(jsonPath("$[1].subjectName").value("Physics"));
    }

    /**
     * Verifies that the endpoint returns {@code 200 OK} and an empty JSON
     * array when the tutor has no reviews.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getReviewsByTutor_noReviews_returns200AndEmptyList() throws Exception {
        when(reviewRepository.findByTutorId(99)).thenReturn(List.of());

        mockMvc.perform(get("/api/reviews/tutor/{tutorId}", 99)
                        .with(authentication(getMockAuth(10, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * Verifies that a review with a missing booking still appears in the
     * response – the subject field is {@code null} and no exception is thrown.
     *
     * <p>This guards against NPE when associated data is partially missing.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getReviewsByTutor_missingBooking_returnsReviewWithNullSubject() throws Exception {
        Integer tutorId = 5;
        Review r = buildReview(1, 999, tutorId, (short) 3, "OK");

        when(reviewRepository.findByTutorId(tutorId)).thenReturn(List.of(r));
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reviews/tutor/{tutorId}", tutorId)
                        .with(authentication(getMockAuth(10, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].subject").doesNotExist());
    }
}