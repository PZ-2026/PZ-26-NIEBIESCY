/*
 * StudentScheduleControllerTest.java
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
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudentScheduleController}.
 *
 * <p>Verifies endpoint security rules, data filtering logic, and response payloads for:
 * <ul>
 * <li>Generating student lesson schedules in PDF format.</li>
 * <li>Filtering lesson entries by specific status, time boundaries, and days of the week.</li>
 * </ul>
 *
 * Uses {@code @ExtendWith(MockitoExtension.class)} for isolated, fast unit testing
 * without loading the entire Spring context.
 *
 * @version 1.0
 * @author EduLink Team
 */
@ExtendWith(MockitoExtension.class)
class StudentScheduleControllerTest {

    /**
     * Mocked repository managing student bookings and reservations.
     */
    @Mock
    private BookingRepository bookingRepository;

    /**
     * Mocked repository managing tutoring offers.
     */
    @Mock
    private OfferRepository offerRepository;

    /**
     * Mocked repository for retrieving user (student and tutor) data.
     */
    @Mock
    private UserRepository userRepository;

    /**
     * Mocked repository managing academic subjects.
     */
    @Mock
    private SubjectRepository subjectRepository;

    /**
     * Mocked repository containing time slot configurations.
     */
    @Mock
    private AvailabilitySlotRepository slotRepository;

    /**
     * The controller instance under test, injected with mocked dependencies.
     */
    @InjectMocks
    private StudentScheduleController studentScheduleController;

    /**
     * Reusable mock user data record representing a student.
     */
    private User student;

    /**
     * Reusable mock user data record representing a tutor.
     */
    private User tutor;

    /**
     * Reusable mock booking instance for active reservation scenarios.
     */
    private Booking acceptedBooking;

    /**
     * Reusable mock offer instance.
     */
    private Offer offer;

    /**
     * Reusable mock slot defining lesson time and day.
     */
    private AvailabilitySlot slot;

    /**
     * Reusable mock subject associated with the tutoring offer.
     */
    private Subject subject;

    /**
     * Prepares standard infrastructure dependencies and initial entity states
     * prior to the execution of individual test cases.
     */
    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1);
        student.setFirstName("Jan");
        student.setLastName("Kowalski");

        tutor = new User();
        tutor.setId(2);
        tutor.setFirstName("Anna");
        tutor.setLastName("Nowak");

        subject = new Subject();
        subject.setId(10);
        subject.setName("Matematyka");

        slot = new AvailabilitySlot();
        slot.setId(20);
        slot.setDayOfWeek((short) 1); // Monday
        slot.setStartTime(LocalTime.of(10, 0));

        offer = new Offer();
        offer.setId(30);
        offer.setSubjectId(subject.getId());
        offer.setTutorId(tutor.getId());

        acceptedBooking = new Booking();
        acceptedBooking.setId(40);
        acceptedBooking.setStudentId(student.getId());
        acceptedBooking.setOfferId(offer.getId());
        acceptedBooking.setAvailabilitySlotId(slot.getId());
        acceptedBooking.setStatusId(6); // ACCEPTED
    }

    // -----------------------------------------------------------------------
    // GET /api/student/{studentId}/schedule/pdf
    // -----------------------------------------------------------------------

    /**
     * Comprehensive validation scenarios targeted directly at PDF schedule generation.
     */
    @Nested
    @DisplayName("GET /api/student/{studentId}/schedule/pdf")
    class GenerateSchedulePdfTests {

        /**
         * Verifies that requesting a schedule with valid active bookings successfully
         * produces a populated PDF byte array with correct HTTP headers.
         *
         * @throws IOException if PDF generation or streaming fails
         */
        @Test
        @DisplayName("Valid active bookings → 200 OK with populated PDF file")
        void generateSchedulePdf_validData_returns200WithPdf() throws IOException {
            when(bookingRepository.findByStudentId(1)).thenReturn(List.of(acceptedBooking));
            when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
            when(slotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
            when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
            when(userRepository.findById(1)).thenReturn(Optional.of(student));

            // Mocking tutor retrieval since includeTutors = true
            when(userRepository.findById(tutor.getId())).thenReturn(Optional.of(tutor));

            ResponseEntity<byte[]> response = studentScheduleController.generateSchedulePdf(
                    1, Collections.emptyList(), true, true, Collections.emptyList()
            );

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
            assertTrue(response.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION));

            byte[] body = response.getBody();
            assertNotNull(body);
            assertTrue(body.length > 0, "Generated PDF payload should not be empty");

            verify(bookingRepository, times(1)).findByStudentId(1);
            verify(userRepository, times(1)).findById(1);
        }

        /**
         * Confirms that pending or rejected bookings are correctly filtered out
         * and do not trigger subsequent database queries.
         *
         * @throws IOException if PDF generation fails
         */
        @Test
        @DisplayName("Pending bookings → Filtered out, no nested queries executed")
        void generateSchedulePdf_pendingBookings_filteredOut() throws IOException {
            Booking pendingBooking = new Booking();
            pendingBooking.setStatusId(3); // PENDING

            when(bookingRepository.findByStudentId(1)).thenReturn(List.of(pendingBooking));
            when(userRepository.findById(1)).thenReturn(Optional.of(student));

            ResponseEntity<byte[]> response = studentScheduleController.generateSchedulePdf(
                    1, null, false, false, null
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(offerRepository, never()).findById(anyInt());
        }

        /**
         * Validates that schedule entries falling outside the standard 08:00 - 17:00
         * time boundary are excluded from the final document.
         *
         * @throws IOException if PDF generation fails
         */
        @Test
        @DisplayName("Late hours (>= 17:00) → Excluded from final schedule")
        void generateSchedulePdf_lateHours_filteredOut() throws IOException {
            slot.setStartTime(LocalTime.of(18, 0)); // Outside bounds

            when(bookingRepository.findByStudentId(1)).thenReturn(List.of(acceptedBooking));
            when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
            when(slotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
            when(userRepository.findById(1)).thenReturn(Optional.of(student));

            ResponseEntity<byte[]> response = studentScheduleController.generateSchedulePdf(
                    1, null, false, false, null
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(subjectRepository, never()).findById(anyInt());
        }

        /**
         * Ensures that providing specific day filters accurately omits
         * bookings scheduled on unrequested days.
         *
         * @throws IOException if PDF generation fails
         */
        @Test
        @DisplayName("Unrequested days → Excluded based on days parameter")
        void generateSchedulePdf_unrequestedDays_filteredOut() throws IOException {
            List<Integer> allowedDays = List.of(2, 3); // Tuesday, Wednesday
            // Setup slot is Monday (1)

            when(bookingRepository.findByStudentId(1)).thenReturn(List.of(acceptedBooking));
            when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
            when(slotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
            when(userRepository.findById(1)).thenReturn(Optional.of(student));

            ResponseEntity<byte[]> response = studentScheduleController.generateSchedulePdf(
                    1, null, false, false, allowedDays
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(subjectRepository, never()).findById(anyInt());
        }
    }
}