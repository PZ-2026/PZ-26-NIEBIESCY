/*
 * OfferController.java
 *
 * Version: 1.3
 * Date: 2026-05-22
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.OfferCreateRequest;
import com.vectorpeaks.backend.dto.OfferDto;
import com.vectorpeaks.backend.dto.SlotDto;
import com.vectorpeaks.backend.entity.*;
import com.vectorpeaks.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing tutoring offers.
 * Provides endpoints to retrieve filtered offers with tutor and subject details.
 *
 * @version 1.3
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/offers")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class OfferController {

    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ReviewRepository reviewRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final OfferSlotRepository offerSlotRepository;

    /**
     * Constructs a new OfferController with all required repositories.
     *
     * @param offerRepository            repository for offers
     * @param userRepository             repository for users (tutors)
     * @param subjectRepository          repository for subjects
     * @param reviewRepository           repository for reviews
     * @param availabilitySlotRepository repository for availability slots
     * @param offerSlotRepository        reposiotory for offer slots
     */
    public OfferController(OfferRepository offerRepository,
                           UserRepository userRepository,
                           SubjectRepository subjectRepository,
                           ReviewRepository reviewRepository,
                           AvailabilitySlotRepository availabilitySlotRepository,
                           OfferSlotRepository offerSlotRepository ) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.reviewRepository = reviewRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.offerSlotRepository = offerSlotRepository;
    }

    /**
     * Retrieves a list of offers optionally filtered by subject, city, online-only flag,
     * or a search text (matching subject or tutor name).
     *
     * @param subject    optional subject name filter
     * @param city       optional city filter
     * @param onlineOnly optional flag to show only online offers
     * @param search     optional search text
     * @return list of OfferDto objects matching the criteria
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<OfferDto> getOffers(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean onlineOnly,
            @RequestParam(required = false) String search
    ) {
        List<Offer> offers = offerRepository.findAll();

        if (subject != null && !subject.isEmpty()) {
            offers = offers.stream()
                    .filter(o -> {
                        Subject s = subjectRepository.findById(o.getSubjectId()).orElse(null);
                        return s != null && s.getName().equalsIgnoreCase(subject);
                    })
                    .collect(Collectors.toList());
        }
        if (city != null && !city.isEmpty()) {
            offers = offers.stream()
                    .filter(o -> {
                        User tutor = userRepository.findById(o.getTutorId()).orElse(null);
                        String tutorCity = tutor != null ? tutor.getAddress() : "";
                        return tutorCity.equalsIgnoreCase(city);
                    })
                    .collect(Collectors.toList());
        }
        if (onlineOnly != null && onlineOnly) {
            offers = offers.stream()
                    .filter(o -> "Online".equalsIgnoreCase(o.getOfferType()))
                    .collect(Collectors.toList());
        }
        if (search != null && !search.isEmpty()) {
            String lowerSearch = search.toLowerCase();
            offers = offers.stream()
                    .filter(o -> {
                        Subject s = subjectRepository.findById(o.getSubjectId()).orElse(null);
                        User tutor = userRepository.findById(o.getTutorId()).orElse(null);
                        String subjectName = s != null ? s.getName().toLowerCase() : "";
                        String tutorName = tutor != null
                                ? (tutor.getFirstName() + " " + tutor.getLastName()).toLowerCase()
                                : "";
                        return subjectName.contains(lowerSearch) || tutorName.contains(lowerSearch);
                    })
                    .collect(Collectors.toList());
        }

        return offers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single offer by its ID.
     *
     * @param id the offer ID
     * @return ResponseEntity containing the OfferDto if found, otherwise 404 Not Found
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OfferDto> getOfferById(@PathVariable Integer id) {
        return offerRepository.findById(id)
                .map(offer -> ResponseEntity.ok(convertToDto(offer)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Converts an Offer entity to an OfferDto containing all necessary fields for the frontend.
     *
     * @param offer the offer entity
     * @return populated OfferDto
     */
    private OfferDto convertToDto(Offer offer) {
        User tutor = userRepository.findById(offer.getTutorId()).orElse(null);
        Subject subject = subjectRepository.findById(offer.getSubjectId()).orElse(null);

        String tutorName = (tutor != null) ? tutor.getFirstName() + " " + tutor.getLastName() : "";
        String city = (tutor != null) ? tutor.getAddress() : "";
        String subjectName = (subject != null) ? subject.getName() : "";
        Double price = offer.getPrice().doubleValue();
        Boolean isOnline = "Online".equalsIgnoreCase(offer.getOfferType());

        List<OfferSlot> offerSlots = offerSlotRepository.findByOfferId(offer.getId());
        List<SlotDto> slots = new ArrayList<>();
        for (OfferSlot offerSlot : offerSlots) {
            AvailabilitySlot slot = availabilitySlotRepository
                    .findById(offerSlot.getAvailabilitySlotId()).orElse(null);
            if (slot != null) {
                String dayName = getDayName(slot.getDayOfWeek());
                String start = slot.getStartTime().toString().substring(0, 5);
                String label = dayName + " " + start;
                slots.add(new SlotDto(slot.getId(), label, slot.getDayOfWeek().intValue()));
            }
        }

        Double avgRating = reviewRepository.getAverageRatingByTutorId(offer.getTutorId());
        Integer reviewCount = reviewRepository.countReviewsByTutorId(offer.getTutorId());
        Float rating = (avgRating != null) ? avgRating.floatValue() : 0f;

        OfferDto dto = new OfferDto();
        dto.setId(offer.getId());
        dto.setTutorId(offer.getTutorId());
        dto.setTutorName(tutorName);
        dto.setSubject(subjectName);
        dto.setDescription(offer.getDetails());
        dto.setPricePerHour(price);
        dto.setCity(city);
        dto.setIsOnline(isOnline);
        dto.setRating(rating);
        dto.setReviewCount(reviewCount);
        dto.setAvailableSlots(slots);
        dto.setIsApproved(offer.getStatusId() == 1);
        dto.setStatus(mapStatusId(offer.getStatusId()));
        return dto;
    }

    /**
     * Converts numeric day-of-week to Polish short name.
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

    /**
     * Retrieves all offers created by a specific tutor.
     *
     * @param tutorId the ID of the tutor
     * @return list of OfferDto objects belonging to the tutor
     */
    @GetMapping("/tutor/{tutorId}")
    @PreAuthorize("isAuthenticated()")
    public List<OfferDto> getOffersByTutor(@PathVariable Integer tutorId) {
        return offerRepository.findAll().stream()
                .filter(o -> o.getTutorId().equals(tutorId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new tutoring offer.
     *
     * @param request the offer creation data (tutorId, subjectId, details,
     *                price, offerType, availabilitySlotIds)
     * @return ResponseEntity with status 200 OK on success, or error message
     */
    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public ResponseEntity<?> createOffer(@RequestBody OfferCreateRequest request) {
        Offer offer = new Offer();
        offer.setTutorId(request.getTutorId());
        offer.setSubjectId(request.getSubjectId());
        offer.setDetails(request.getDetails());
        offer.setPrice(request.getPrice());
        offer.setOfferType(request.getOfferType());
        offer.setStatusId(3);
        offer.setGlobalLimitId(1);
        Offer saved = offerRepository.save(offer);

        if (request.getAvailabilitySlotIds() != null) {
            for (Integer slotId : request.getAvailabilitySlotIds()) {
                OfferSlot offerSlot = new OfferSlot();
                offerSlot.setOfferId(saved.getId());
                offerSlot.setAvailabilitySlotId(slotId);
                offerSlotRepository.save(offerSlot);
            }
        }

        return ResponseEntity.ok().build();
    }

    /**
    * Deletes an offer by its ID.
     * @param id the unique identifier of the offer to delete
     * @return  ResponseEntity with status  200 OK if deletion was successful,
     *          or 404 Not Found if the offer does not exist
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteOffer(@PathVariable Integer id) {
        if (!offerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        offerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    /**
     *  Updates an existing offer with the given ID.
     *  @param id      the unique identifier of the offer to update
     *  @param request the object containing new offer data (tutorId is ignored, statusId is fixed)
     *  @return ResponseEntity with status  200 OK on successful update,
     *          or 404 Not Found if the offer does not exist
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateOffer(@PathVariable Integer id,
                                         @RequestBody OfferCreateRequest request) {
        Optional<Offer> offerOpt = offerRepository.findById(id);
        if (offerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Offer offer = offerOpt.get();
        offer.setSubjectId(request.getSubjectId());
        offer.setDetails(request.getDetails());
        offer.setPrice(request.getPrice());
        offer.setOfferType(request.getOfferType());
        offer.setStatusId(3);
        offerRepository.save(offer);

        if (request.getAvailabilitySlotIds() != null) {
            offerSlotRepository.deleteByOfferId(id);
            for (Integer slotId : request.getAvailabilitySlotIds()) {
                OfferSlot offerSlot = new OfferSlot();
                offerSlot.setOfferId(id);
                offerSlot.setAvailabilitySlotId(slotId);
                offerSlotRepository.save(offerSlot);
            }
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Maps an internal status ID to a human-readable status name.
     * @param statusId the numeric status identifier (e.g., 1, 3, 7)
     * @return the corresponding status string, or "UNKNOWN" if the ID is not recognized
     */
    private String mapStatusId(Integer statusId) {
        switch (statusId) {
            case 1: return "ACTIVE";
            case 3: return "PENDING";
            case 7: return "REJECTED";
            default: return "UNKNOWN";
        }
    }
}