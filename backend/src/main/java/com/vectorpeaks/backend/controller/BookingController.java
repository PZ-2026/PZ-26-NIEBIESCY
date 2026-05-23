/*
 * BookingController.java
 *
 * Version: 1.1
 * Date: 2026-05-22
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing bookings (lesson reservations).
 * Provides endpoints to create bookings and retrieve bookings for a specific student.
 *
 * @version 1.1
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class BookingController {

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
     * @param offerSlotRepository repository for offers slots
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

        AvailabilitySlot slot = slotRepository.findById(booking.getAvailabilitySlotId()).orElse(null);
        String date = "";
        String time = "";
        if (slot != null) {
            date = booking.getBookingDate() != null ? booking.getBookingDate().toLocalDate().toString() : "";
            time = slot.getStartTime().toString().substring(0, 5);
        }

        Short rating = null;
        if (booking.getId() != null) {
            Optional<Review> reviewOpt = reviewRepository.findByBookingId(booking.getId().longValue());
            if (reviewOpt.isPresent()) {
                rating = reviewOpt.get().getRating();
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
     * @param status    new status string (ACCEPTED or REJECTED)
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Integer bookingId,
            @RequestParam String status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Booking booking = bookingOpt.get();
        switch (status) {
            case "ACCEPTED": booking.setStatusId(6); break;
            case "REJECTED": booking.setStatusId(7); break;
            case "COMPLETED": booking.setStatusId(4); break;
            default: return ResponseEntity.badRequest().body("Unknown status: " + status);
        }
        bookingRepository.save(booking);
        return ResponseEntity.ok().build();
    }
}