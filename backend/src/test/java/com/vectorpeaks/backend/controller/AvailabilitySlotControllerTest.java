/*
 * AvailabilitySlotControllerTest.java
 *
 * Version: 1.2
 * Date: 2026-05-28
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.entity.AvailabilitySlot;
import com.vectorpeaks.backend.entity.Offer;
import com.vectorpeaks.backend.entity.OfferSlot;
import com.vectorpeaks.backend.repository.AvailabilitySlotRepository;
import com.vectorpeaks.backend.repository.OfferRepository;
import com.vectorpeaks.backend.repository.OfferSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link AvailabilitySlotController}.
 *
 * <p>Verifies the behaviour of the following endpoints:
 * <ul>
 *   <li>{@code GET /api/slots} – retrieve all availability slots,</li>
 *   <li>{@code GET /api/slots/available/{tutorId}} – slots not used by a tutor,</li>
 *   <li>{@code GET /api/slots/available/{tutorId}/excluding/{offerId}} – slots not
 *       used by a tutor's other offers.</li>
 * </ul>
 *
 * <p>Uses {@code @WebMvcTest} with {@link MockMvc} – only the controller layer
 * is loaded; no full Spring context or database is required.
 * All repository dependencies are replaced by Mockito mocks ({@code @MockitoBean}).
 *
 * <p>{@code MaintenanceService} and {@link com.vectorpeaks.backend.repository.UserRepository}
 * are mocked and stubbed in {@link BaseControllerTest} – no redeclaration needed here.
 *
 * @version 1.2
 * @author EduLink Team
 * @see AvailabilitySlotController
 */
@WebMvcTest(
        controllers = AvailabilitySlotController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
class AvailabilitySlotControllerTest extends BaseControllerTest {

    /** HTTP client used to perform requests in web-layer tests. */
    @Autowired
    private MockMvc mockMvc;

    /** Mock of the availability slot repository. */
    @MockitoBean
    private AvailabilitySlotRepository slotRepository;

    /** Mock of the offer repository. */
    @MockitoBean
    private OfferRepository offerRepository;

    /** Mock of the offer slot repository. */
    @MockitoBean
    private OfferSlotRepository offerSlotRepository;

    /** Slot on Monday 10:00 used across multiple tests. */
    private AvailabilitySlot slotMon10;

    /** Slot on Wednesday 14:00 used across multiple tests. */
    private AvailabilitySlot slotWed14;

    /**
     * Sets up shared test fixtures before each test case:
     * two availability slots for Monday and Wednesday.
     * The {@code AccessInterceptor} bypass is handled by
     * {@link BaseControllerTest#setUpInterceptorBypass()}.
     */
    @BeforeEach
    void setUp() {
        slotMon10 = buildSlot(1, (short) 1, LocalTime.of(10, 0));
        slotWed14 = buildSlot(2, (short) 3, LocalTime.of(14, 0));
    }

    // -----------------------------------------------------------------------
    // Helper: authenticated request
    // -----------------------------------------------------------------------

    /**
     * Creates a {@link UsernamePasswordAuthenticationToken} for injecting into
     * MockMvc requests. The controller class is annotated with
     * {@code @PreAuthorize("isAuthenticated()")}, so all endpoints require
     * an authenticated principal; without it every request returns 401.
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

    // -----------------------------------------------------------------------
    // GET /api/slots
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns {@code 200 OK} and all slots
     * with correct labels and day-of-week values.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getAllSlots_slotsExist_returns200AndList() throws Exception {
        when(slotRepository.findAll()).thenReturn(List.of(slotMon10, slotWed14));

        mockMvc.perform(get("/api/slots")
                        .with(authentication(getMockAuth(1, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].label").value("Pon 10:00"))
                .andExpect(jsonPath("$[0].dayOfWeek").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].label").value("Śr 14:00"))
                .andExpect(jsonPath("$[1].dayOfWeek").value(3));
    }

    /**
     * Verifies that the endpoint returns {@code 200 OK} and an empty array
     * when no slots exist.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getAllSlots_noSlots_returns200AndEmptyList() throws Exception {
        when(slotRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/slots")
                        .with(authentication(getMockAuth(1, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * Verifies the day label mapping for all days of the week.
     * Controller maps: 0→Nd, 1→Pon, 2→Wt, 3→Śr, 4→Czw, 5→Pt, 6→Sob.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getAllSlots_dayLabelMapping_isCorrect() throws Exception {
        short[] days = {0, 1, 2, 3, 4, 5, 6};
        String[] expectedPrefixes = {"Nd", "Pon", "Wt", "Śr", "Czw", "Pt", "Sob"};

        for (int i = 0; i < days.length; i++) {
            AvailabilitySlot slot = buildSlot(10 + i, days[i], LocalTime.of(9, 0));
            when(slotRepository.findAll()).thenReturn(List.of(slot));

            mockMvc.perform(get("/api/slots")
                            .with(authentication(getMockAuth(1, "ROLE_STUDENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].label").value(expectedPrefixes[i] + " 09:00"));
        }
    }

    // -----------------------------------------------------------------------
    // GET /api/slots/available/{tutorId}
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint returns all slots when the tutor has no offers
     * (no slots are occupied).
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getAvailableSlotsForTutor_noOffers_returnsAllSlots() throws Exception {
        when(slotRepository.findAll()).thenReturn(List.of(slotMon10, slotWed14));
        when(offerRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/slots/available/5")
                        .with(authentication(getMockAuth(1, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    /**
     * Verifies that slots already used in any offer by the tutor are excluded
     * from the result.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getAvailableSlotsForTutor_oneSlotUsed_returnsRemainingSlots() throws Exception {
        Offer offer = buildOffer(10, 5);
        OfferSlot usedOfferSlot = buildOfferSlot(10, 1); // slot ID 1 (Pon 10:00) is occupied

        when(slotRepository.findAll()).thenReturn(List.of(slotMon10, slotWed14));
        when(offerRepository.findAll()).thenReturn(List.of(offer));
        when(offerSlotRepository.findByOfferId(10)).thenReturn(List.of(usedOfferSlot));

        mockMvc.perform(get("/api/slots/available/5")
                        .with(authentication(getMockAuth(1, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].label").value("Śr 14:00"));
    }

    /**
     * Verifies that offers belonging to other tutors do not affect the result —
     * only the requested tutor's offers are considered.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getAvailableSlotsForTutor_otherTutorOffers_areIgnored() throws Exception {
        Offer otherTutorOffer = buildOffer(20, 99); // tutorId = 99, not 5
        OfferSlot otherOfferSlot = buildOfferSlot(20, 1);

        when(slotRepository.findAll()).thenReturn(List.of(slotMon10, slotWed14));
        when(offerRepository.findAll()).thenReturn(List.of(otherTutorOffer));
        when(offerSlotRepository.findByOfferId(20)).thenReturn(List.of(otherOfferSlot));

        // tutor 5 has no offers, so both slots should be available
        mockMvc.perform(get("/api/slots/available/5")
                        .with(authentication(getMockAuth(1, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // -----------------------------------------------------------------------
    // GET /api/slots/available/{tutorId}/excluding/{offerId}
    // -----------------------------------------------------------------------

    /**
     * Verifies that the endpoint excludes slots used by the tutor's OTHER offers,
     * but includes slots used by the excluded offer itself (so they can be
     * re-selected when editing that offer).
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getAvailableSlotsExcludingOffer_excludesOtherOffersSlots() throws Exception {
        Offer currentOffer = buildOffer(10, 5);  // offer being edited – uses slot 1
        Offer otherOffer   = buildOffer(11, 5);  // another offer of the same tutor – uses slot 2

        OfferSlot otherOfferSlot = buildOfferSlot(11, 2);

        when(slotRepository.findAll()).thenReturn(List.of(slotMon10, slotWed14));
        when(offerRepository.findAll()).thenReturn(List.of(currentOffer, otherOffer));
        when(offerSlotRepository.findByOfferId(11)).thenReturn(List.of(otherOfferSlot));

        // Slot 2 (Śr 14:00) is used by another offer → must not appear.
        // Slot 1 (Pon 10:00) belongs to the excluded offer → must be available.
        mockMvc.perform(get("/api/slots/available/5/excluding/10")
                        .with(authentication(getMockAuth(1, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].label").value("Pon 10:00"));
    }

    /**
     * Verifies that when the tutor has no other offers, all slots are returned
     * (the excluded offer's slots are not blocked).
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void getAvailableSlotsExcludingOffer_noOtherOffers_returnsAllSlots() throws Exception {
        Offer currentOffer = buildOffer(10, 5);

        when(slotRepository.findAll()).thenReturn(List.of(slotMon10, slotWed14));
        when(offerRepository.findAll()).thenReturn(List.of(currentOffer));

        mockMvc.perform(get("/api/slots/available/5/excluding/10")
                        .with(authentication(getMockAuth(1, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Creates an {@link AvailabilitySlot} with the given ID, day, and start time.
     *
     * @param id        slot identifier
     * @param dayOfWeek day of the week (0 = Sunday … 6 = Saturday)
     * @param startTime start time of the slot
     * @return populated {@link AvailabilitySlot}
     */
    private AvailabilitySlot buildSlot(Integer id, Short dayOfWeek, LocalTime startTime) {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(id);
        slot.setDayOfWeek(dayOfWeek);
        slot.setStartTime(startTime);
        return slot;
    }

    /**
     * Creates an {@link Offer} with the given ID and tutor ID.
     *
     * @param id      offer identifier
     * @param tutorId tutor identifier
     * @return populated {@link Offer}
     */
    private Offer buildOffer(Integer id, Integer tutorId) {
        Offer o = new Offer();
        o.setId(id);
        o.setTutorId(tutorId);
        return o;
    }

    /**
     * Creates an {@link OfferSlot} linking an offer to a slot.
     *
     * @param offerId            offer identifier
     * @param availabilitySlotId slot identifier
     * @return populated {@link OfferSlot}
     */
    private OfferSlot buildOfferSlot(Integer offerId, Integer availabilitySlotId) {
        OfferSlot os = new OfferSlot();
        os.setOfferId(offerId);
        os.setAvailabilitySlotId(availabilitySlotId);
        return os;
    }
}