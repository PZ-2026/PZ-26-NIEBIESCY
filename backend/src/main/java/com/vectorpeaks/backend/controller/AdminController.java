/*
 * AdminController.java
 *
 * Version: 1.1
 * Date: 2026-05-10
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.*;
import com.vectorpeaks.backend.entity.*;
import com.vectorpeaks.backend.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for admin-specific operations.
 * Provides endpoints for dashboard statistics, pending bookings and offers,
 * reports, subject management, and global settings.
 *
 * @version 1.1
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
    private final EntityManager entityManager;

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
     * @param entityManager          JPA entity manager for native queries
     */
    public AdminController(UserRepository userRepository,
                           OfferRepository offerRepository,
                           BookingRepository bookingRepository,
                           SubjectRepository subjectRepository,
                           ReviewRepository reviewRepository,
                           GlobalLimitRepository globalLimitRepository,
                           AvailabilitySlotRepository slotRepository,
                           EntityManager entityManager) {
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
        this.bookingRepository = bookingRepository;
        this.subjectRepository = subjectRepository;
        this.reviewRepository = reviewRepository;
        this.globalLimitRepository = globalLimitRepository;
        this.slotRepository = slotRepository;
        this.entityManager = entityManager;
    }

    // ==================== DASHBOARD ====================

    /**
     * Returns dashboard statistics for the admin panel.
     *
     * @return AdminStatsResponse with user, offer, booking, and pending counts
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
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
        stats.setPendingOffersCount(offerRepository.findByStatusId(3).size());
        return stats;
    }

    /**
     * Returns a list of pending bookings for admin review.
     *
     * @return list of BookingResponse objects with status PENDING
     */
    @GetMapping("/bookings/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponse> getPendingBookings() {
        List<Booking> pendingBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatusId() != null && b.getStatusId() == 3)
                .collect(Collectors.toList());

        return pendingBookings.stream()
                .map(this::convertToBookingResponse)
                .collect(Collectors.toList());
    }

    // ==================== PENDING OFFERS ====================

    /**
     * Returns a list of pending offers awaiting admin approval.
     * Pending offers have status_id = 3 (Pending).
     *
     * @return list of OfferDto objects with status PENDING
     */
    @GetMapping("/offers/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OfferDto> getPendingOffers() {
        List<Offer> pendingOffers = offerRepository.findByStatusId(3);
        return pendingOffers.stream()
                .map(this::convertToOfferDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of an offer (approve or reject).
     *
     * @param id     the offer ID
     * @param status new status string: ACCEPTED or REJECTED
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/offers/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOfferStatus(@PathVariable Integer id,
                                                @RequestParam String status) {
        Optional<Offer> offerOpt = offerRepository.findById(id);
        if (offerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Offer offer = offerOpt.get();
        switch (status) {
            case "ACCEPTED":
                offer.setStatusId(1); // Active
                break;
            case "REJECTED":
                offer.setStatusId(7); // Rejected
                break;
            default:
                return ResponseEntity.badRequest().body("Unknown status: " + status);
        }
        offerRepository.save(offer);
        return ResponseEntity.ok().build();
    }

    // ==================== SUBJECT MANAGEMENT ====================

    /**
     * Creates a new subject with Active status.
     *
     * @param body map containing "name" key with the subject name
     * @return ResponseEntity with the created subject or error
     */
    @Transactional
    @PostMapping("/subjects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addSubject(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body("Nazwa przedmiotu jest wymagana");
        }

        // Check for duplicates
        boolean exists = subjectRepository.findAll().stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(name.trim()));
        if (exists) {
            return ResponseEntity.badRequest().body("Przedmiot o takiej nazwie już istnieje");
        }

        // Use native SQL INSERT because subjects table has no auto-increment
        Integer maxId = subjectRepository.findAll().stream()
                .map(Subject::getId)
                .max(Integer::compareTo)
                .orElse(0);
        int newId = maxId + 1;

        entityManager.createNativeQuery(
                "INSERT INTO subjects (id, name, status_id) VALUES (:id, :name, :statusId)")
                .setParameter("id", newId)
                .setParameter("name", name.trim())
                .setParameter("statusId", 1)
                .executeUpdate();

        return ResponseEntity.ok(new SubjectDto(newId, name.trim()));
    }

    /**
     * Deletes a subject by its ID.
     *
     * @param id the subject ID
     * @return ResponseEntity with success or error
     */
    @DeleteMapping("/subjects/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSubject(@PathVariable Integer id) {
        if (!subjectRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Check if subject is used by any offers
        boolean isUsed = offerRepository.findAll().stream()
                .anyMatch(o -> o.getSubjectId().equals(id));
        if (isUsed) {
            return ResponseEntity.badRequest()
                    .body("Nie można usunąć przedmiotu, który jest używany w ofertach");
        }

        subjectRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }


    // ==================== REPORTS ====================

    /**
     * Returns report data including booking/offer counts and popular subjects.
     *
     * @return AdminReportsResponse with statistics and popular subjects
     */
    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
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

    // ==================== SETTINGS ====================

    /**
     * Retrieves global platform settings.
     *
     * @return GlobalLimitDto with current settings, or defaults if none exist
     */
    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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

    // ==================== CONVERTERS ====================

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
     * Converts an Offer entity to an OfferDto for the admin pending offers view.
     *
     * @param offer the offer entity
     * @return populated OfferDto
     */
    private OfferDto convertToOfferDto(Offer offer) {
        User tutor = userRepository.findById(offer.getTutorId()).orElse(null);
        Subject subject = subjectRepository.findById(offer.getSubjectId()).orElse(null);

        String tutorName = (tutor != null) ? tutor.getFirstName() + " " + tutor.getLastName() : "";
        String subjectName = (subject != null) ? subject.getName() : "";
        String city = (tutor != null) ? tutor.getAddress() : "";

        OfferDto dto = new OfferDto();
        dto.setId(offer.getId());
        dto.setTutorId(offer.getTutorId());
        dto.setTutorName(tutorName);
        dto.setSubject(subjectName);
        dto.setDescription(offer.getDetails());
        dto.setPricePerHour(offer.getPrice().doubleValue());
        dto.setCity(city);
        dto.setIsOnline("Online".equalsIgnoreCase(offer.getOfferType()));
        dto.setIsApproved(false);

        Double avgRating = reviewRepository.getAverageRatingByTutorId(offer.getTutorId());
        Integer reviewCount = reviewRepository.countReviewsByTutorId(offer.getTutorId());
        dto.setRating(avgRating != null ? avgRating.floatValue() : 0f);
        dto.setReviewCount(reviewCount != null ? reviewCount : 0);

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
