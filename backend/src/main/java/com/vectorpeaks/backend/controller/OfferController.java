/*
 * OfferController.java
 *
 * Version: 1.0
 * Date: 2026-04-26
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.OfferDto;
import com.vectorpeaks.backend.entity.AvailabilitySlot;
import com.vectorpeaks.backend.entity.Offer;
import com.vectorpeaks.backend.entity.Subject;
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.AvailabilitySlotRepository;
import com.vectorpeaks.backend.repository.OfferRepository;
import com.vectorpeaks.backend.repository.ReviewRepository;
import com.vectorpeaks.backend.repository.SubjectRepository;
import com.vectorpeaks.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing tutoring offers.
 * Provides endpoints to retrieve filtered offers with tutor and subject details.
 *
 * @version 1.0
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

    /**
     * Constructs a new OfferController with all required repositories.
     *
     * @param offerRepository            repository for offers
     * @param userRepository             repository for users (tutors)
     * @param subjectRepository          repository for subjects
     * @param reviewRepository           repository for reviews
     * @param availabilitySlotRepository repository for availability slots
     */
    public OfferController(OfferRepository offerRepository,
                           UserRepository userRepository,
                           SubjectRepository subjectRepository,
                           ReviewRepository reviewRepository,
                           AvailabilitySlotRepository availabilitySlotRepository) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.reviewRepository = reviewRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
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
                        String tutorName = tutor != null ? (tutor.getFirstName() + " " + tutor.getLastName()).toLowerCase() : "";
                        return subjectName.contains(lowerSearch) || tutorName.contains(lowerSearch);
                    })
                    .collect(Collectors.toList());
        }

        return offers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
        String description = offer.getDetails();
        Double price = offer.getPrice().doubleValue();
        Boolean isOnline = "Online".equalsIgnoreCase(offer.getOfferType());

        String slotStr = "";
        if (offer.getAvailabilitySlotId() != null) {
            AvailabilitySlot slot = availabilitySlotRepository.findById(offer.getAvailabilitySlotId()).orElse(null);
            if (slot != null) {
                String dayName = getDayName(slot.getDayOfWeek());
                slotStr = dayName + " " + slot.getStartTime().toString().substring(0, 5);
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
        dto.setDescription(description);
        dto.setPricePerHour(price);
        dto.setCity(city);
        dto.setIsOnline(isOnline);
        dto.setRating(rating);
        dto.setReviewCount(reviewCount);
        dto.setAvailableSlots(slotStr.isEmpty() ? List.of() : List.of(slotStr));
        dto.setIsApproved(true);
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
}