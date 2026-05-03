/*
 * BookingControllerTest.java
 *
 * Version: 1.0
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorpeaks.backend.dto.BookingRequest;
import com.vectorpeaks.backend.entity.*;
import com.vectorpeaks.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link BookingController}.
 *
 * <p>Verifies the behaviour of the following endpoints:
 * <ul>
 *   <li>{@code GET /api/bookings/student/{studentId}} – retrieve bookings for a student,</li>
 *   <li>{@code POST /api/bookings} – create a new booking.</li>
 * </ul>
 *
 * <p>Uses {@code @WebMvcTest} with {@link MockMvc} – only the controller layer
 * is loaded; no full Spring context or database is required.
 * All repository dependencies are replaced by Mockito mocks
 * ({@code @MockitoBean}).
 *
 * @version 1.0
 * @author EduLink Team
 * @see BookingController
 */
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    /** HTTP client used to perform requests in web-layer tests. */
    @Autowired
    private MockMvc mockMvc;

    /** JSON mapper used to serialize request objects. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Mock of the booking repository. */
    @MockitoBean
    private BookingRepository bookingRepository;

    /** Mock of the offer repository. */
    @MockitoBean
    private OfferRepository offerRepository;

    /** Mock of the subject repository. */
    @MockitoBean
    private SubjectRepository subjectRepository;

    /** Mock of the user repository. */
    @MockitoBean
    private UserRepository userRepository;

    /** Mock of the availability slot repository. */
    @MockitoBean
    private AvailabilitySlotRepository slotRepository;

    /** Mock of the review repository. */
    @MockitoBean
    private ReviewRepository reviewRepository;

    /** Sample offer used across multiple tests. */
    private Offer mockOffer;

    /** Sample booking used across multiple tests. */
    private Booking mockBooking;

    /**
     * Sets up shared test fixtures before each test case:
     * a sample offer and a sample booking linked to student ID 10.
     */
    @BeforeEach
    void setUp() {
        mockOffer = new Offer();
        mockOffer.setId(1);
        mockOffer.setTutorId(5);
        mockOffer.setSubjectId(2);
        mockOffer.setAvailabilitySlotId(3);
        mockOffer.setPrice(BigDecimal.valueOf(80.00));
        mockOffer.setOfferType("Online");
        mockOffer.setDetails("Mathematics tutoring");

        mockBooking = new Booking();
        mockBooking.setId(100);
        mockBooking.setStudentId(10);
        mockBooking.setOfferId(1);
        mockBooking.setAvailabilitySlotId(3);
        mockBooking.setStatusId(3);
        mockBooking.setBookingDate(LocalDateTime.of(2026, 5, 10, 14, 0));
    }

    // -----------------------------------------------------------------------
    // GET /api/bookings/student/{studentId}
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 200 OK} and a JSON array
     * with one booking when the student has a booking in the database.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getBookingsForStudent_bookingExists_returns200AndList() throws Exception {
        User tutor = buildUser(5, "Adam", "Wiśniewski");
        Subject subject = buildSubject(2, "Mathematics");
        AvailabilitySlot slot = buildSlot(3, (short) 1, LocalTime.of(14, 0));

        when(bookingRepository.findByStudentId(10)).thenReturn(List.of(mockBooking));
        when(offerRepository.findById(1)).thenReturn(Optional.of(mockOffer));
        when(userRepository.findById(5)).thenReturn(Optional.of(tutor));
        when(subjectRepository.findById(2)).thenReturn(Optional.of(subject));
        when(slotRepository.findById(3)).thenReturn(Optional.of(slot));
        when(reviewRepository.findByBookingId(100L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bookings/student/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].subject").value("Mathematics"))
                .andExpect(jsonPath("$[0].tutorName").value("Adam Wiśniewski"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    /**
     * Verifies that the endpoint returns {@code 200 OK} and an empty JSON
     * array when the student has no bookings.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getBookingsForStudent_noBookings_returns200AndEmptyList() throws Exception {
        when(bookingRepository.findByStudentId(99)).thenReturn(List.of());

        mockMvc.perform(get("/api/bookings/student/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * Verifies that the booking status is mapped correctly for each known
     * status ID: 3 → PENDING, 6 → ACCEPTED, 7 → REJECTED, 4 → COMPLETED.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getBookingsForStudent_statusMapping_isParsedCorrectly() throws Exception {
        User tutor = buildUser(5, "Adam", "Wiśniewski");
        Subject subject = buildSubject(2, "Mathematics");
        AvailabilitySlot slot = buildSlot(3, (short) 1, LocalTime.of(14, 0));

        int[][] statusCases = {{3, 0}, {6, 1}, {7, 2}, {4, 3}};
        String[] expectedLabels = {"PENDING", "ACCEPTED", "REJECTED", "COMPLETED"};

        for (int i = 0; i < statusCases.length; i++) {
            Booking b = new Booking();
            b.setId(200 + i);
            b.setStudentId(20);
            b.setOfferId(1);
            b.setAvailabilitySlotId(3);
            b.setStatusId(statusCases[i][0]);
            b.setBookingDate(LocalDateTime.now());

            when(bookingRepository.findByStudentId(20)).thenReturn(List.of(b));
            when(offerRepository.findById(1)).thenReturn(Optional.of(mockOffer));
            when(userRepository.findById(5)).thenReturn(Optional.of(tutor));
            when(subjectRepository.findById(2)).thenReturn(Optional.of(subject));
            when(slotRepository.findById(3)).thenReturn(Optional.of(slot));
            when(reviewRepository.findByBookingId((long)(200 + i))).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/bookings/student/20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value(expectedLabels[i]));
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/bookings
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 200 OK} when a valid booking
     * request is submitted with an existing offer and matching slot.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void createBooking_validRequest_returns200() throws Exception {
        when(offerRepository.existsById(1)).thenReturn(true);
        when(offerRepository.findById(1)).thenReturn(Optional.of(mockOffer));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);

        BookingRequest request = buildBookingRequest(1, 10, 3);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that the endpoint returns {@code 400 Bad Request}
     * when the offer ID does not exist in the database.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void createBooking_offerNotFound_returns400() throws Exception {
        when(offerRepository.existsById(999)).thenReturn(false);

        BookingRequest request = buildBookingRequest(999, 10, 3);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Offer not found"));
    }

    /**
     * Verifies that the endpoint returns {@code 400 Bad Request}
     * when the provided slot ID does not match the slot assigned to the offer.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void createBooking_slotMismatch_returns400() throws Exception {
        when(offerRepository.existsById(1)).thenReturn(true);
        when(offerRepository.findById(1)).thenReturn(Optional.of(mockOffer));

        // slot ID 99 does not match offer's slot ID 3
        BookingRequest request = buildBookingRequest(1, 10, 99);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Slot does not belong to this offer"));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Creates a {@link BookingRequest} with the given IDs.
     *
     * @param offerId             offer identifier
     * @param studentId           student identifier
     * @param availabilitySlotId  availability slot identifier
     * @return populated {@link BookingRequest}
     */
    private BookingRequest buildBookingRequest(Integer offerId, Integer studentId,
                                               Integer availabilitySlotId) {
        BookingRequest r = new BookingRequest();
        r.setOfferId(offerId);
        r.setStudentId(studentId);
        r.setAvailabilitySlotId(availabilitySlotId);
        return r;
    }

    /**
     * Creates a {@link User} with the given ID and name – helper for test fixtures.
     *
     * @param id        user identifier
     * @param firstName first name
     * @param lastName  last name
     * @return populated {@link User}
     */
    private User buildUser(Integer id, String firstName, String lastName) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        return u;
    }

    /**
     * Creates a {@link Subject} with the given ID and name.
     *
     * @param id   subject identifier
     * @param name subject name
     * @return populated {@link Subject}
     */
    private Subject buildSubject(Integer id, String name) {
        Subject s = new Subject();
        s.setId(id);
        s.setName(name);
        return s;
    }

    /**
     * Creates an {@link AvailabilitySlot} with the given ID, day, and start time.
     *
     * @param id         slot identifier
     * @param dayOfWeek  day of the week (0 = Sunday … 6 = Saturday)
     * @param startTime  start time of the slot
     * @return populated {@link AvailabilitySlot}
     */
    private AvailabilitySlot buildSlot(Integer id, Short dayOfWeek, LocalTime startTime) {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(id);
        slot.setDayOfWeek(dayOfWeek);
        slot.setStartTime(startTime);
        return slot;
    }
}