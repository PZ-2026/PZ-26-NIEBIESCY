/*
 * TutorScheduleControllerTest.java
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
 * Unit tests for {@link TutorScheduleController}.
 *
 * <p>Verifies endpoint logic, data filtering, and response payloads for:
 * <ul>
 * <li>Generating tutor lesson schedules in PDF format.</li>
 * <li>Filtering bookings by specific subject criteria.</li>
 * <li>Filtering lesson entries by specific status, time boundaries, and days of the week.</li>
 * </ul>
 *
 * Uses {@code @ExtendWith(MockitoExtension.class)} for isolated, fast unit testing
 * without loading the entire Spring application context.
 *
 * @version 1.0
 * @author EduLink Team
 */
@ExtendWith(MockitoExtension.class)
class TutorScheduleControllerTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private OfferRepository offerRepository;
    @Mock private UserRepository userRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private AvailabilitySlotRepository slotRepository;

    @InjectMocks
    private TutorScheduleController tutorScheduleController;

    private User tutor;
    private User student;
    private Booking activeBooking;
    private Offer offer;
    private AvailabilitySlot slot;
    private Subject subject;

    /**
     * Prepares standard infrastructure dependencies and initial entity states
     * prior to the execution of individual test cases.
     */
    @BeforeEach
    void setUp() {
        // Prepare tutor test data
        tutor = new User();
        tutor.setId(1);
        tutor.setFirstName("Adam");
        tutor.setLastName("Kowalski");

        // Prepare student test data
        student = new User();
        student.setId(2);
        student.setFirstName("Ewa");
        student.setLastName("Nowak");

        // Prepare subject data
        subject = new Subject();
        subject.setId(10);
        subject.setName("Physics");

        // Prepare slot (Monday, 10:00 - falls within the 8-17 range)
        slot = new AvailabilitySlot();
        slot.setId(20);
        slot.setDayOfWeek((short) 1);
        slot.setStartTime(LocalTime.of(10, 0));

        // Prepare offer data
        offer = new Offer();
        offer.setId(30);
        offer.setSubjectId(subject.getId());
        offer.setTutorId(tutor.getId());

        // Prepare active booking (status 6 = ACCEPTED)
        activeBooking = new Booking();
        activeBooking.setId(40);
        activeBooking.setStudentId(student.getId());
        activeBooking.setOfferId(offer.getId());
        activeBooking.setAvailabilitySlotId(slot.getId());
        activeBooking.setStatusId(6);
    }

    // -----------------------------------------------------------------------
    // GET /api/tutor/{tutorId}/schedule/pdf
    // -----------------------------------------------------------------------

    /**
     * Comprehensive validation scenarios targeted directly at tutor PDF schedule generation.
     */
    @Nested
    @DisplayName("GET /api/tutor/{tutorId}/schedule/pdf")
    class GenerateSchedulePdfTests {

        /**
         * Verifies that requesting a schedule with valid active bookings successfully
         * produces a populated PDF byte array with correct HTTP headers.
         *
         * @throws IOException if PDF generation fails
         */
        @Test
        @DisplayName("Valid active bookings → 200 OK with populated PDF file")
        void generateSchedulePdf_validData_returns200WithPdf() throws IOException {
            // Arrange
            Integer tutorId = 1;

            // Note: Controller uses findAll() to filter data in memory.
            when(offerRepository.findAll()).thenReturn(List.of(offer));
            when(bookingRepository.findAll()).thenReturn(List.of(activeBooking));

            // Mocks for looking up details inside the loop
            when(slotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
            when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
            when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
            when(userRepository.findById(tutorId)).thenReturn(Optional.of(tutor));

            // includeStudents flag is true, so we also mock the student retrieval
            when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

            // Act
            ResponseEntity<byte[]> response = tutorScheduleController.generateSchedulePdf(
                    tutorId,
                    Collections.emptyList(), // subjectIds
                    true,                    // includeStudents
                    true,                    // includeTotalHours
                    Collections.emptyList()  // days
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
         * Confirms that bookings that are neither accepted nor pending 
         * are correctly filtered out and do not trigger subsequent database queries.
         *
         * @throws IOException if PDF generation fails
         */
        @Test
        @DisplayName("Completed/Cancelled bookings → Filtered out")
        void generateSchedulePdf_statusOtherThanAcceptedOrPending_filteredOut() throws IOException {
            // Arrange
            Booking completedBooking = new Booking();
            completedBooking.setOfferId(offer.getId());
            completedBooking.setStatusId(4); // 4 = COMPLETED

            when(offerRepository.findAll()).thenReturn(List.of(offer));
            when(bookingRepository.findAll()).thenReturn(List.of(completedBooking));
            when(userRepository.findById(1)).thenReturn(Optional.of(tutor));

            // Act
            ResponseEntity<byte[]> response = tutorScheduleController.generateSchedulePdf(
                    1, null, false, false, null
            );

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            // Slot lookup should not be called for filtered out bookings
            verify(slotRepository, never()).findById(anyInt());
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
            // Arrange
            slot.setStartTime(LocalTime.of(18, 0)); // Outside bounds

            when(offerRepository.findAll()).thenReturn(List.of(offer));
            when(bookingRepository.findAll()).thenReturn(List.of(activeBooking));
            when(slotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
            when(userRepository.findById(1)).thenReturn(Optional.of(tutor));

            // Act
            ResponseEntity<byte[]> response = tutorScheduleController.generateSchedulePdf(
                    1, null, false, false, null
            );

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            // Since the time filtered out the entry, subject lookup should not happen
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
            // Arrange
            List<Integer> allowedDays = List.of(2, 3); // Tuesday, Wednesday
            // Setup slot is Monday (1)

            when(offerRepository.findAll()).thenReturn(List.of(offer));
            when(bookingRepository.findAll()).thenReturn(List.of(activeBooking));
            when(slotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
            when(userRepository.findById(1)).thenReturn(Optional.of(tutor));

            // Act
            ResponseEntity<byte[]> response = tutorScheduleController.generateSchedulePdf(
                    1, null, false, false, allowedDays
            );

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            // Entry rejected by days filter, so subject retrieval should not be called
            verify(subjectRepository, never()).findById(anyInt());
        }

        /**
         * Ensures that providing subject filters correctly ignores offers
         * matching subjects that the tutor didn't select.
         *
         * @throws IOException if PDF generation fails
         */
        @Test
        @DisplayName("Unrequested subjects → Ignored during offer fetching")
        void generateSchedulePdf_unrequestedSubject_filteredOut() throws IOException {
            // Arrange
            List<Integer> selectedSubjects = List.of(99); // Request subject 99
            // Offer belongs to subject 10, so it should be skipped

            when(offerRepository.findAll()).thenReturn(List.of(offer));
            when(bookingRepository.findAll()).thenReturn(List.of(activeBooking));
            when(userRepository.findById(1)).thenReturn(Optional.of(tutor));

            // Act
            ResponseEntity<byte[]> response = tutorScheduleController.generateSchedulePdf(
                    1, selectedSubjects, false, false, null
            );

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            // The offer wasn't collected into tutorOfferIds, so the booking was skipped.
            // Slot lookup should never be reached.
            verify(slotRepository, never()).findById(anyInt());
        }
    }
}