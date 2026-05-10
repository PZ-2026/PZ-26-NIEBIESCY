/*
 * AdminController.java
 *
 * Version: 1.0
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.*;
import com.vectorpeaks.backend.entity.*;
import com.vectorpeaks.backend.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for admin-specific operations.
 * Provides endpoints for dashboard statistics, pending bookings,
 * reports, and global settings management.
 *
 * @version 1.0
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class AdminController {

    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;
    private final SubjectRepository subjectRepository;
    private final ReviewRepository reviewRepository;
    private final GlobalLimitRepository globalLimitRepository;
    private final AvailabilitySlotRepository slotRepository;

    /**
     * Constructs a new AdminController with all required repositories.
     *
     * @param userRepository         repository for users
     * @param offerRepository        repository for offers
     * @param bookingRepository      repository for bookings
     * @param subjectRepository      repository for subjects
     * @param reviewRepository       repository for reviews
     * @param globalLimitRepository  repository for global limits
     * @param slotRepository         repository for availability slots
     */
    public AdminController(UserRepository userRepository,
                           OfferRepository offerRepository,
                           BookingRepository bookingRepository,
                           SubjectRepository subjectRepository,
                           ReviewRepository reviewRepository,
                           GlobalLimitRepository globalLimitRepository,
                           AvailabilitySlotRepository slotRepository) {
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
        this.bookingRepository = bookingRepository;
        this.subjectRepository = subjectRepository;
        this.reviewRepository = reviewRepository;
        this.globalLimitRepository = globalLimitRepository;
        this.slotRepository = slotRepository;
    }

    /**
     * Returns dashboard statistics for the admin panel.
     *
     * @return AdminStatsResponse with user, offer, booking, and pending counts
     */
    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        AdminStatsResponse stats = new AdminStatsResponse();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalOffers(offerRepository.count());
        stats.setTotalBookings(bookingRepository.count());
        stats.setTutorsCount(userRepository.findAll().stream()
                .filter(u -> u.getRoleId() != null && u.getRoleId() == 2).count());
        stats.setStudentsCount(userRepository.findAll().stream()
                .filter(u -> u.getRoleId() != null && u.getRoleId() == 3).count());
        stats.setPendingCount(bookingRepository.findAll().stream()
                .filter(b -> b.getStatusId() != null && b.getStatusId() == 3).count());
        return stats;
    }

    /**
     * Returns a list of pending bookings for admin review.
     *
     * @return list of BookingResponse objects with status PENDING
     */
    @GetMapping("/bookings/pending")
    public List<BookingResponse> getPendingBookings() {
        List<Booking> pendingBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatusId() != null && b.getStatusId() == 3)
                .collect(Collectors.toList());

        return pendingBookings.stream()
                .map(this::convertToBookingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns report data including booking/offer counts and popular subjects.
     *
     * @return AdminReportsResponse with statistics and popular subjects
     */
    @GetMapping("/reports")
    public AdminReportsResponse getReports() {
        AdminReportsResponse reports = new AdminReportsResponse();
        reports.setTotalBookings(bookingRepository.count());
        reports.setTotalOffers(offerRepository.count());

        List<Subject> allSubjects = subjectRepository.findAll();
        List<SubjectEntry> popularSubjects = new ArrayList<>();

        for (Subject subject : allSubjects) {
            List<Offer> subjectOffers = offerRepository.findAll().stream()
                    .filter(o -> o.getSubjectId().equals(subject.getId()))
                    .collect(Collectors.toList());

            int totalReviews = 0;
            for (Offer offer : subjectOffers) {
                Integer count = reviewRepository.countReviewsByTutorId(offer.getTutorId());
                totalReviews += (count != null ? count : 0);
            }

            SubjectEntry entry = new SubjectEntry();
            entry.setName(subject.getName());
            entry.setReviewCount(totalReviews);
            popularSubjects.add(entry);
        }

        popularSubjects.sort((a, b) -> Integer.compare(b.getReviewCount(), a.getReviewCount()));
        reports.setPopularSubjects(popularSubjects);
        return reports;
    }

    /**
     * Retrieves global platform settings.
     *
     * @return GlobalLimitDto with current settings, or defaults if none exist
     */
    @GetMapping("/settings")
    public GlobalLimitDto getSettings() {
        GlobalLimitDto dto = new GlobalLimitDto();
        Optional<GlobalLimit> limitOpt = globalLimitRepository.findById(1);
        if (limitOpt.isPresent()) {
            GlobalLimit limit = limitOpt.get();
            dto.setMaxPricePerHour(limit.getHourlyPriceLimit() != null
                    ? limit.getHourlyPriceLimit().doubleValue() : 200.0);
            dto.setGlobalMessage(limit.getMessage() != null ? limit.getMessage() : "");
        } else {
            dto.setMaxPricePerHour(200.0);
            dto.setGlobalMessage("");
        }
        return dto;
    }

    /**
     * Updates global platform settings.
     *
     * @param dto the new settings to save
     * @return ResponseEntity with success status
     */
    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody GlobalLimitDto dto) {
        GlobalLimit limit = globalLimitRepository.findById(1).orElse(new GlobalLimit());
        limit.setId(1);
        if (dto.getMaxPricePerHour() != null) {
            limit.setHourlyPriceLimit(BigDecimal.valueOf(dto.getMaxPricePerHour()));
        }
        limit.setMessage(dto.getGlobalMessage());
        globalLimitRepository.save(limit);
        return ResponseEntity.ok().build();
    }

    /**
     * Converts a Booking entity to a BookingResponse DTO.
     *
     * @param booking the booking entity
     * @return populated BookingResponse
     */
    private BookingResponse convertToBookingResponse(Booking booking) {
        Offer offer = offerRepository.findById(booking.getOfferId()).orElse(null);
        if (offer == null) return null;

        User tutor = userRepository.findById(offer.getTutorId()).orElse(null);
        User student = userRepository.findById(booking.getStudentId()).orElse(null);
        Subject subject = subjectRepository.findById(offer.getSubjectId()).orElse(null);

        String tutorName = (tutor != null) ? tutor.getFirstName() + " " + tutor.getLastName() : "";
        String studentName = (student != null) ? student.getFirstName() + " " + student.getLastName() : "";
        String subjectName = (subject != null) ? subject.getName() : "";

        AvailabilitySlot slot = slotRepository.findById(booking.getAvailabilitySlotId()).orElse(null);
        String date = "";
        String time = "";
        if (slot != null) {
            date = booking.getBookingDate() != null ? booking.getBookingDate().toLocalDate().toString() : "";
            time = slot.getStartTime().toString().substring(0, 5);
        }

        BookingResponse dto = new BookingResponse();
        dto.setId(booking.getId());
        dto.setOfferId(offer.getId());
        dto.setSubject(subjectName);
        dto.setTutorName(tutorName);
        dto.setDate(date);
        dto.setTime(time);
        dto.setPrice(offer.getPrice().doubleValue());
        dto.setStatus(mapStatusId(booking.getStatusId()));
        dto.setTutorId(offer.getTutorId());
        return dto;
    }

    /**
     * Maps numeric status ID to a human-readable string.
     *
     * @param statusId the status code
     * @return status string
     */
    private String mapStatusId(Integer statusId) {
        switch (statusId) {
            case 3: return "PENDING";
            case 6: return "ACCEPTED";
            case 7: return "REJECTED";
            case 4: return "COMPLETED";
            default: return "UNKNOWN";
        }
    }
}
