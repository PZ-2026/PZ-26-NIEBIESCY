/*
 * OfferControllerTest.java
 *
 * Version: 1.0
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.entity.AvailabilitySlot;
import com.vectorpeaks.backend.entity.Offer;
import com.vectorpeaks.backend.entity.Subject;
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link OfferController}.
 *
 * <p>Verifies the behaviour of the following endpoints:
 * <ul>
 *   <li>{@code GET /api/offers} – retrieve all offers with optional filters,</li>
 *   <li>{@code GET /api/offers/{id}} – retrieve a single offer by ID.</li>
 * </ul>
 *
 * <p>Uses {@code @WebMvcTest} with {@link MockMvc} – only the controller layer
 * is loaded; no full Spring context or database is required.
 * All repository dependencies are replaced by Mockito mocks
 * ({@code @MockitoBean}).
 *
 * @version 1.0
 * @author EduLink Team
 * @see OfferController
 */
@WebMvcTest(OfferController.class)
class OfferControllerTest {

    /** HTTP client used to perform requests in web-layer tests. */
    @Autowired
    private MockMvc mockMvc;

    /** Mock of the offer repository. */
    @MockitoBean
    private OfferRepository offerRepository;

    /** Mock of the user repository. */
    @MockitoBean
    private UserRepository userRepository;

    /** Mock of the subject repository. */
    @MockitoBean
    private SubjectRepository subjectRepository;

    /** Mock of the review repository. */
    @MockitoBean
    private ReviewRepository reviewRepository;

    /** Mock of the availability slot repository. */
    @MockitoBean
    private AvailabilitySlotRepository availabilitySlotRepository;

    /** Sample online offer used across multiple tests. */
    private Offer onlineOffer;

    /** Sample in-person offer used across multiple tests. */
    private Offer inPersonOffer;

    /**
     * Sets up shared test fixtures before each test case.
     * Initialises two offers (online and in-person), a tutor, a subject,
     * a slot, and stubs common repository calls used by {@code convertToDto}.
     */
    @BeforeEach
    void setUp() {
        onlineOffer = buildOffer(1, 10, 2, 3, "Online",  BigDecimal.valueOf(80));
        inPersonOffer = buildOffer(2, 10, 2, 4, "InPerson", BigDecimal.valueOf(60));

        User tutor = buildUser(10, "Anna", "Kowalska", "Warszawa");
        Subject subject = buildSubject(2, "Mathematics");
        AvailabilitySlot slot3 = buildSlot(3, (short) 1, LocalTime.of(10, 0));
        AvailabilitySlot slot4 = buildSlot(4, (short) 3, LocalTime.of(14, 0));

        when(userRepository.findById(10)).thenReturn(Optional.of(tutor));
        when(subjectRepository.findById(2)).thenReturn(Optional.of(subject));
        when(availabilitySlotRepository.findById(3)).thenReturn(Optional.of(slot3));
        when(availabilitySlotRepository.findById(4)).thenReturn(Optional.of(slot4));
        when(reviewRepository.getAverageRatingByTutorId(10)).thenReturn(4.5);
        when(reviewRepository.countReviewsByTutorId(10)).thenReturn(12);
    }

    // -----------------------------------------------------------------------
    // GET /api/offers – no filters
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 200 OK} and all offers
     * when no filter parameters are provided.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getOffers_noFilters_returnsAllOffers() throws Exception {
        when(offerRepository.findAll()).thenReturn(List.of(onlineOffer, inPersonOffer));

        mockMvc.perform(get("/api/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    /**
     * Verifies that the endpoint returns {@code 200 OK} and an empty array
     * when no offers exist in the database.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getOffers_noOffers_returns200AndEmptyList() throws Exception {
        when(offerRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -----------------------------------------------------------------------
    // GET /api/offers – subject filter
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint filters offers by subject name (case-insensitive)
     * and returns only the matching offer.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getOffers_subjectFilter_returnsMatchingOffers() throws Exception {
        Subject mathSubject = buildSubject(2, "Mathematics");
        when(offerRepository.findAll()).thenReturn(List.of(onlineOffer, inPersonOffer));
        when(subjectRepository.findById(2)).thenReturn(Optional.of(mathSubject));

        mockMvc.perform(get("/api/offers").param("subject", "Mathematics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subject").value("Mathematics"));
    }

    // -----------------------------------------------------------------------
    // GET /api/offers – city filter
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint filters offers by city (tutor address,
     * case-insensitive) and returns only the offer whose tutor is in Warszawa.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getOffers_cityFilter_returnsMatchingOffers() throws Exception {
        when(offerRepository.findAll()).thenReturn(List.of(onlineOffer, inPersonOffer));

        mockMvc.perform(get("/api/offers").param("city", "Warszawa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Warszawa"));
    }

    // -----------------------------------------------------------------------
    // GET /api/offers – onlineOnly filter
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns only online offers when
     * the {@code onlineOnly=true} parameter is provided.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getOffers_onlineOnlyFilter_returnsOnlyOnlineOffers() throws Exception {
        when(offerRepository.findAll()).thenReturn(List.of(onlineOffer, inPersonOffer));

        mockMvc.perform(get("/api/offers").param("onlineOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isOnline").value(true));
    }

    // -----------------------------------------------------------------------
    // GET /api/offers – search filter
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns offers matching the search text
     * against both subject name and tutor full name (case-insensitive).
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getOffers_searchFilter_returnsMatchingOffers() throws Exception {
        when(offerRepository.findAll()).thenReturn(List.of(onlineOffer, inPersonOffer));

        mockMvc.perform(get("/api/offers").param("search", "anna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tutorName").value("Anna Kowalska"));
    }

    // -----------------------------------------------------------------------
    // GET /api/offers/{id}
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 200 OK} and the correct OfferDto
     * when an offer with the given ID exists.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getOfferById_offerExists_returns200AndDto() throws Exception {
        when(offerRepository.findById(1)).thenReturn(Optional.of(onlineOffer));

        mockMvc.perform(get("/api/offers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tutorName").value("Anna Kowalska"))
                .andExpect(jsonPath("$.subject").value("Mathematics"))
                .andExpect(jsonPath("$.pricePerHour").value(80.0))
                .andExpect(jsonPath("$.isOnline").value(true));
    }

    /**
     * Verifies that the endpoint returns {@code 404 Not Found}
     * when no offer with the given ID exists.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getOfferById_offerNotFound_returns404() throws Exception {
        when(offerRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/offers/999"))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifies that the OfferDto returned by the endpoint contains
     * the correct rating and review count from the review repository.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getOfferById_returnsRatingAndReviewCount() throws Exception {
        when(offerRepository.findById(1)).thenReturn(Optional.of(onlineOffer));

        mockMvc.perform(get("/api/offers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.reviewCount").value(12));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Creates an {@link Offer} with the given data.
     *
     * @param id                 offer identifier
     * @param tutorId            tutor identifier
     * @param subjectId          subject identifier
     * @param availabilitySlotId slot identifier
     * @param offerType          offer type string (e.g. "Online")
     * @param price              price per hour
     * @return populated {@link Offer}
     */
    private Offer buildOffer(Integer id, Integer tutorId, Integer subjectId,
                             Integer availabilitySlotId, String offerType,
                             BigDecimal price) {
        Offer o = new Offer();
        o.setId(id);
        o.setTutorId(tutorId);
        o.setSubjectId(subjectId);
        o.setAvailabilitySlotId(availabilitySlotId);
        o.setOfferType(offerType);
        o.setPrice(price);
        o.setDetails("Sample description");
        return o;
    }

    /**
     * Creates a {@link User} representing a tutor with the given data.
     *
     * @param id        user identifier
     * @param firstName first name
     * @param lastName  last name
     * @param address   city / address used for city filtering
     * @return populated {@link User}
     */
    private User buildUser(Integer id, String firstName, String lastName, String address) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setAddress(address);
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
     * Creates an {@link AvailabilitySlot} with the given data.
     *
     * @param id        slot identifier
     * @param dayOfWeek day of the week (0 = Sunday … 6 = Saturday)
     * @param startTime slot start time
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