/*
 * BookingController.java
 *
 * Version: 1.4
 * Date: 2026-06-08
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.BookingRequest;
import com.vectorpeaks.backend.dto.BookingResponse;
import com.vectorpeaks.backend.entity.*;
import com.vectorpeaks.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing bookings (lesson reservations).
 * Provides endpoints to create bookings and retrieve bookings for a specific student.
 * Includes built-in BOLA (Broken Object Level Authorization) protection and security event logging.
 *
 * @version 1.4
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingRepository bookingRepository;
    private final OfferRepository offerRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final ReviewRepository reviewRepository;
    private final OfferSlotRepository offerSlotRepository;

    /**
     * Constructs a new BookingController with all required repositories.
     *
     * @param bookingRepository   repository for bookings
     * @param offerRepository     repository for offers
     * @param subjectRepository   repository for subjects
     * @param userRepository      repository for users
     * @param slotRepository      repository for availability slots
     * @param reviewRepository    repository for reviews
     * @param offerSlotRepository repository for offer slots
     */
    public BookingController(BookingRepository bookingRepository,
                             OfferRepository offerRepository,
                             SubjectRepository subjectRepository,
                             UserRepository userRepository,
                             AvailabilitySlotRepository slotRepository,
                             ReviewRepository reviewRepository,
                             OfferSlotRepository offerSlotRepository) {
        this.bookingRepository = bookingRepository;
        this.offerRepository = offerRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.reviewRepository = reviewRepository;
        this.offerSlotRepository = offerSlotRepository;
    }

    /**
     * Retrieves all bookings for a given student.
     *
     * @param studentId the ID of the student
     * @return list of BookingResponse objects containing booking details
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("#studentId == authentication.principal and hasRole('STUDENT') or hasRole('ADMIN')")
    public List<BookingResponse> getBookingsForStudent(@PathVariable Integer studentId) {
        List<Booking> bookings = bookingRepository.findByStudentId(studentId);
        return bookings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts a Booking entity to a BookingResponse DTO.
     *
     * @param booking the booking entity
     * @return populated BookingResponse or null if offer not found
     */
    private BookingResponse convertToDto(Booking booking) {
        Offer offer = offerRepository.findById(booking.getOfferId()).orElse(null);
        if (offer == null) return null;

        User tutor = userRepository.findById(offer.getTutorId()).orElse(null);
        Subject subject = subjectRepository.findById(offer.getSubjectId()).orElse(null);

        String tutorName = (tutor != null) ? tutor.getFirstName() + " " + tutor.getLastName() : "";
        String subjectName = (subject != null) ? subject.getName() : "";

        User student = userRepository.findById(booking.getStudentId()).orElse(null);
        String studentName = (student != null) ? student.getFirstName() + " " + student.getLastName() : "";

        AvailabilitySlot slot = slotRepository.findById(booking.getAvailabilitySlotId()).orElse(null);
        String date = "";
        String time = "";
        if (slot != null) {
            date = getDayName(slot.getDayOfWeek());
            time = slot.getStartTime().toString().substring(0, 5);
        }

        Short rating = null;
        String reviewComment = null;
        if (booking.getId() != null) {
            Optional<Review> reviewOpt = reviewRepository.findByBookingId(booking.getId());
            if (reviewOpt.isPresent()) {
                rating = reviewOpt.get().getRating();
                reviewComment = reviewOpt.get().getComment();
            }
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
        dto.setRating(rating);
        dto.setTutorId(offer.getTutorId());
        dto.setReviewComment(reviewComment);
        dto.setStudentId(booking.getStudentId());
        dto.setStudentName(studentName);
        return dto;
    }

    /**
     * Maps numeric status ID to a human-readable string.
     *
     * @param statusId the status code
     * @return status string (PENDING, ACCEPTED, REJECTED, COMPLETED, or UNKNOWN)
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

    /**
     * Creates a new booking.
     *
     * @param request the booking request containing offer ID, student ID, and availability slot ID
     * @return ResponseEntity with success or error message
     */
    @PostMapping
    @PreAuthorize("(#request.studentId == authentication.principal and hasRole('STUDENT')) or hasRole('ADMIN')")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        if (!offerRepository.existsById(request.getOfferId())) {
            return ResponseEntity.badRequest().body("Offer not found");
        }

        boolean slotBelongsToOffer = offerSlotRepository
                .findByOfferId(request.getOfferId())
                .stream()
                .anyMatch(os -> os.getAvailabilitySlotId().equals(request.getAvailabilitySlotId()));

        if (!slotBelongsToOffer) {
            return ResponseEntity.badRequest().body("Slot does not belong to this offer");
        }

        boolean slotAlreadyBooked = bookingRepository
                .findByOfferIdAndAvailabilitySlotId(request.getOfferId(), request.getAvailabilitySlotId())
                .stream()
                .anyMatch(b -> b.getStatusId() == 3 || b.getStatusId() == 6);

        if (slotAlreadyBooked) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                    .body("Slot is already booked.");
        }

        Booking booking = new Booking();
        booking.setOfferId(request.getOfferId());
        booking.setStudentId(request.getStudentId());
        booking.setAvailabilitySlotId(request.getAvailabilitySlotId());
        booking.setStatusId(3);
        booking.setBookingDate(java.time.LocalDateTime.now());

        bookingRepository.save(booking);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves all bookings for a given tutor.
     *
     * @param tutorId the ID of the tutor
     * @return list of BookingResponse objects containing booking details
     */
    @GetMapping("/tutor/{tutorId}")
    @PreAuthorize("(#tutorId == authentication.principal and hasRole('TUTOR')) or hasRole('ADMIN')")
    public List<BookingResponse> getBookingsForTutor(@PathVariable Integer tutorId) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> {
                    Offer offer = offerRepository.findById(b.getOfferId()).orElse(null);
                    return offer != null && offer.getTutorId().equals(tutorId);
                })
                .collect(Collectors.toList());
        return bookings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a booking.
     *
     * @param bookingId the ID of the booking
     * @param status    new status string (ACCEPTED, REJECTED, or COMPLETED)
     * @param authentication the security context containing the logged-in user's details
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/{bookingId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Integer bookingId,
            @RequestParam String status,
            org.springframework.security.core.Authentication authentication) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Booking booking = bookingOpt.get();

        Integer loggedInUserId = (Integer) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Offer offer = offerRepository.findById(booking.getOfferId()).orElse(null);
        if (offer == null) {
            return ResponseEntity.badRequest().body("Associated offer not found");
        }

        if (!isAdmin && !offer.getTutorId().equals(loggedInUserId)) {
            logger.warn("SECURITY ALERT (BOLA): User ID {} attempted to update booking ID {} belonging to Tutor ID {}",
                    loggedInUserId, bookingId, offer.getTutorId());
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body("You do not have permission to change the status of this booking.");
        }

        switch (status) {
            case "ACCEPTED": booking.setStatusId(6); break;
            case "REJECTED": booking.setStatusId(7); break;
            case "COMPLETED": booking.setStatusId(4); break;
            default: return ResponseEntity.badRequest().body("Unknown status: " + status);
        }
        bookingRepository.save(booking);
        return ResponseEntity.ok().build();
    }

    /**
     * Marks a booking as completed by the student.
     *
     * @param bookingId the ID of the booking
     * @param authentication the security context containing the logged-in user's details
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/{bookingId}/complete")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> completeBooking(
            @PathVariable Integer bookingId,
            org.springframework.security.core.Authentication authentication) {

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Booking booking = bookingOpt.get();

        Integer loggedInUserId = (Integer) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !booking.getStudentId().equals(loggedInUserId)) {
            logger.warn("SECURITY ALERT (BOLA): User ID {} attempted to complete Booking ID {} belonging to Student ID {}",
                    loggedInUserId, bookingId, booking.getStudentId());
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body("You do not have permission to complete this booking.");
        }

        if (booking.getStatusId() != 6) {
            return ResponseEntity.badRequest().body("Booking must be accepted before it can be completed.");
        }

        booking.setStatusId(4);
        bookingRepository.save(booking);
        return ResponseEntity.ok().build();
    }

    /**
     * Converts a numeric day-of-week value to its Polish full name.
     *
     * @param dayOfWeek day number (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
     * @return Polish name of the day, or empty string if the value is unrecognized
     */

    private String getDayName(Short dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return "Poniedziałek";
            case 2: return "Wtorek";
            case 3: return "Środa";
            case 4: return "Czwartek";
            case 5: return "Piątek";
            case 6: return "Sobota";
            case 0: return "Niedziela";
            default: return "";
        }
    }
}