/*
 * TutorReportControllerTest.java
 *
 * Version: 1.0
 * Date: 2026-05-29
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.entity.*;
import com.vectorpeaks.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TutorReportController}.
 *
 * <p>Verifies data aggregation, filtering logic, and response payloads for:
 * <ul>
 * <li>Generating statistical tutor reports in PDF format.</li>
 * <li>Filtering data based on provided date ranges.</li>
 * <li>Conditionally including or excluding report sections (students, subjects, reviews).</li>
 * </ul>
 *
 * Uses {@code @ExtendWith(MockitoExtension.class)} for isolated unit testing
 * without loading the Spring application context.
 *
 * @version 1.0
 * @author EduLink Team
 */
@ExtendWith(MockitoExtension.class)
class TutorReportControllerTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private OfferRepository offerRepository;
    @Mock private UserRepository userRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private ReviewRepository reviewRepository;

    @InjectMocks
    private TutorReportController tutorReportController;

    private User tutor;
    private User student;
    private Subject subject;
    private Offer offer;
    private Booking booking;
    private Review review;

    /**
     * Prepares standardized entity states and mock objects
     * required for report generation scenarios.
     */
    @BeforeEach
    void setUp() {
        tutor = new User();
        tutor.setId(1);
        tutor.setFirstName("Adam");
        tutor.setLastName("Kowalski");

        student = new User();
        student.setId(2);
        student.setFirstName("Ewa");
        student.setLastName("Nowak");

        subject = new Subject();
        subject.setId(10);
        subject.setName("Physics");

        offer = new Offer();
        offer.setId(100);
        offer.setTutorId(tutor.getId());
        offer.setSubjectId(subject.getId());

        booking = new Booking();
        booking.setId(1000);
        booking.setStudentId(student.getId());
        booking.setOfferId(offer.getId());
        booking.setStatusId(6); // 6 = ACCEPTED
        booking.setBookingDate(LocalDateTime.now().minusDays(5)); // 5 days ago

        review = new Review();
        review.setId(500);
        review.setTutorId(tutor.getId());
        review.setBookingId(1000);
        review.setRating((short) 5);
        review.setComment("Great tutor!");
        review.setCreatedAt(LocalDateTime.now().minusDays(2));
    }

    // -----------------------------------------------------------------------
    // GET /api/tutor/{tutorId}/reports/pdf
    // -----------------------------------------------------------------------

    /**
     * Test suite focusing on PDF report generation scenarios.
     */
    @Nested
    @DisplayName("GET /api/tutor/{tutorId}/reports/pdf")
    class GenerateTutorPdfReportTests {

        /**
         * Verifies that generating a full report with default parameters successfully
         * processes all data and produces a valid PDF byte array.
         *
         * @throws IOException if PDF document construction fails
         */
        @Test
        @DisplayName("Valid active data → 200 OK with populated PDF report")
        void generateTutorPdfReport_validData_returns200WithPdf() throws IOException {
            // Arrange
            Integer tutorId = 1;
            when(offerRepository.findAll()).thenReturn(List.of(offer));
            when(bookingRepository.findAll()).thenReturn(List.of(booking));
            when(reviewRepository.getAverageRatingByTutorId(tutorId)).thenReturn(4.8);
            when(reviewRepository.findAll()).thenReturn(List.of(review));

            // Mock individual lookups needed for rows mapping
            when(userRepository.findById(tutorId)).thenReturn(Optional.of(tutor));
            when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
            when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
            when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
            when(bookingRepository.findById(1000)).thenReturn(Optional.of(booking));

            // Act
            // Passing null dates to trigger default 30-day lookback window
            ResponseEntity<byte[]> response = tutorReportController.generateTutorPdfReport(
                    tutorId,
                    null, null,   // dates
                    true, true,   // includeStudents, includeSubjects
                    Collections.emptyList(), // subjectIds
                    true, 5       // includeReviews, reviews limit
            );

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
            assertTrue(response.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION));

            byte[] body = response.getBody();
            assertNotNull(body);
            assertTrue(body.length > 0, "Generated PDF payload should not be empty");
        }

        /**
         * Ensures that bookings occurring outside the specified date range 
         * are completely filtered out from the final report calculations.
         *
         * @throws IOException if PDF document construction fails
         */
        @Test
        @DisplayName("Data outside date bounds → Filtered out from report")
        void generateTutorPdfReport_dataOutsideDateRange_isFilteredOut() throws IOException {
            // Arrange
            // Booking is from 5 days ago, we request report from 40 to 30 days ago
            LocalDate from = LocalDate.now().minusDays(40);
            LocalDate to = LocalDate.now().minusDays(30);

            when(offerRepository.findAll()).thenReturn(List.of(offer));
            when(bookingRepository.findAll()).thenReturn(List.of(booking));
            // Review queries are still executed for the review section unless disabled
            when(reviewRepository.findAll()).thenReturn(Collections.emptyList());
            when(userRepository.findById(1)).thenReturn(Optional.of(tutor));

            // Act
            ResponseEntity<byte[]> response = tutorReportController.generateTutorPdfReport(
                    1, from, to,
                    true, true, Collections.emptyList(), true, 5
            );

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());

            // Since the booking was filtered out due to the date range, 
            // the system shouldn't try to look up the student details for the student list.
            verify(userRepository, never()).findById(student.getId());
        }

        /**
         * Validates that disabling optional sections correctly prevents
         * unnecessary database queries and data processing.
         *
         * @throws IOException if PDF document construction fails
         */
        @Test
        @DisplayName("Sections disabled (include=false) → Skips redundant DB queries")
        void generateTutorPdfReport_sectionsDisabled_skipsQueries() throws IOException {
            // Arrange
            when(offerRepository.findAll()).thenReturn(List.of(offer));
            when(bookingRepository.findAll()).thenReturn(List.of(booking));
            when(userRepository.findById(1)).thenReturn(Optional.of(tutor));

            // Act
            ResponseEntity<byte[]> response = tutorReportController.generateTutorPdfReport(
                    1,
                    null, null,
                    false, // includeStudents
                    false, // includeSubjects
                    Collections.emptyList(),
                    false, // includeReviews
                    5
            );

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());

            // Verifying that disabled sections prevent corresponding repository calls
            verify(reviewRepository, never()).findAll();
            verify(userRepository, never()).findById(student.getId());
            verify(subjectRepository, never()).findById(anyInt());
        }
    }
}