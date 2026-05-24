/*
 * AvailabilitySlotController.java
 *
 * Version: 1.3
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */
package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.SlotDto;
import com.vectorpeaks.backend.entity.AvailabilitySlot;
import com.vectorpeaks.backend.entity.Offer;
import com.vectorpeaks.backend.entity.OfferSlot;
import com.vectorpeaks.backend.repository.AvailabilitySlotRepository;
import com.vectorpeaks.backend.repository.OfferRepository;
import com.vectorpeaks.backend.repository.OfferSlotRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing availability slots.
 * Provides endpoints to retrieve global slots defined by the admin,
 * as well as slots specific to a given tutor or offer.
 *
 * @version 1.2
 * @author EduLink Team
 */
@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/slots")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class AvailabilitySlotController {

    private final AvailabilitySlotRepository slotRepository;
    private final OfferRepository offerRepository;
    private final OfferSlotRepository offerSlotRepository;

    /**
     * Constructs a new AvailabilitySlotController with required repositories.
     *
     * @param slotRepository repository for availability slots
     * @param offerRepository repository for offers
     * @param offerSlotRepository repository for offer slots
     */
    public AvailabilitySlotController(AvailabilitySlotRepository slotRepository,
                                      OfferRepository offerRepository,
                                      OfferSlotRepository offerSlotRepository) {
        this.slotRepository = slotRepository;
        this.offerRepository = offerRepository;
        this.offerSlotRepository = offerSlotRepository;
    }

    /**
     * Retrieves all available time slots as DTOs with human-readable labels.
     *
     * @return list of {@link SlotDto} objects
     */
    @GetMapping
    public List<SlotDto> getAllSlots() {
        return slotRepository.findAll().stream()
                .map(slot -> new SlotDto(
                        slot.getId(),
                        formatSlotLabel(slot),
                        slot.getDayOfWeek().intValue(),
                        false))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all available time slots for a specific tutor,
     * excluding those already occupied by any of their active offers.
     *
     * @param tutorId the ID of the tutor
     * @return list of available {@link SlotDto} objects
     */
    @GetMapping("/available/{tutorId}")
    public List<SlotDto> getAvailableSlotsForTutor(@PathVariable Integer tutorId) {
        List<Integer> usedSlotIds = offerRepository.findAll().stream()
                .filter(o -> o.getTutorId().equals(tutorId))
                .flatMap(o -> offerSlotRepository.findByOfferId(o.getId()).stream())
                .map(OfferSlot::getAvailabilitySlotId)
                .collect(Collectors.toList());

        return slotRepository.findAll().stream()
                .filter(slot -> !usedSlotIds.contains(slot.getId()))
                .map(slot -> new SlotDto(
                        slot.getId(),
                        formatSlotLabel(slot),
                        slot.getDayOfWeek().intValue(),
                        false))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all available time slots for a specific tutor,
     * excluding slots occupied by their other offers (ignoring the current one).
     * Useful when updating an existing offer.
     *
     * @param tutorId the ID of the tutor
     * @param offerId the ID of the offer to be ignored
     * @return list of available {@link SlotDto} objects
     */
    @GetMapping("/available/{tutorId}/excluding/{offerId}")
    public List<SlotDto> getAvailableSlotsExcludingOffer(
            @PathVariable Integer tutorId,
            @PathVariable Integer offerId) {

        List<Integer> usedSlotIds = offerRepository.findAll().stream()
                .filter(o -> o.getTutorId().equals(tutorId) && !o.getId().equals(offerId))
                .flatMap(o -> offerSlotRepository.findByOfferId(o.getId()).stream())
                .map(OfferSlot::getAvailabilitySlotId)
                .collect(Collectors.toList());

        return slotRepository.findAll().stream()
                .filter(slot -> !usedSlotIds.contains(slot.getId()))
                .map(slot -> new SlotDto(
                        slot.getId(),
                        formatSlotLabel(slot),
                        slot.getDayOfWeek().intValue(),
                        false))
                .collect(Collectors.toList());
    }

    /**
     * Formats the slot as a human-readable string (e.g., "Pon 10:00").
     *
     * @param slot the slot entity
     * @return formatted label string
     */
    private String formatSlotLabel(AvailabilitySlot slot) {
        String dayName = getDayName(slot.getDayOfWeek());
        String start = slot.getStartTime().toString().substring(0, 5);
        return dayName + " " + start;
    }

    /**
     * Converts a numeric day-of-week representation to a Polish short name.
     *
     * @param dayOfWeek day number (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
     * @return abbreviated day name
     */
    private String getDayName(Short dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return "Pon";
            case 2: return "Wt";
            case 3: return "Śr";
            case 4: return "Czw";
            case 5: return "Pt";
            case 6: return "Sob";
            case 0: return "Nd";
            default: return "";
        }
    }
}