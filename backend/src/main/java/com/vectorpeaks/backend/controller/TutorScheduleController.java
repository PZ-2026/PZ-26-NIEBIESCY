/*
 * TutorScheduleController.java
 *
 * Version: 1.0
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.pdf.EduLinkSchedulePdfReport;
import com.vectorpeaks.pdf.EduLinkSchedulePdfReport.ScheduleEntry;
import com.vectorpeaks.backend.repository.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for generating a PDF schedule for a tutor.
 * Displays active bookings (accepted or pending) in a weekly timetable.
 *
 * @version 1.0
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/tutor")
@CrossOrigin(origins = "*")
public class TutorScheduleController {

    private final BookingRepository       bookingRepository;
    private final OfferRepository         offerRepository;
    private final UserRepository          userRepository;
    private final SubjectRepository       subjectRepository;
    private final AvailabilitySlotRepository slotRepository;

    /**
     * Constructs a new TutorScheduleController with required repositories.
     *
     * @param bookingRepository repository for bookings
     * @param offerRepository   repository for offers
     * @param userRepository    repository for users
     * @param subjectRepository repository for subjects
     * @param slotRepository    repository for availability slots
     */
    public TutorScheduleController(BookingRepository bookingRepository,
                                   OfferRepository offerRepository,
                                   UserRepository userRepository,
                                   SubjectRepository subjectRepository,
                                   AvailabilitySlotRepository slotRepository) {
        this.bookingRepository = bookingRepository;
        this.offerRepository   = offerRepository;
        this.userRepository    = userRepository;
        this.subjectRepository = subjectRepository;
        this.slotRepository    = slotRepository;
    }

    /**
     * Generates a PDF schedule for a tutor.
     *
     * @param tutorId          the ID of the tutor
     * @param subjectIds       optional list of subject IDs to filter (empty = all)
     * @param includeStudents  whether to show student names in the schedule
     * @param includeTotalHours whether to show total hours summary under the table
     * @param days             optional list of days of week to filter (0=Sunday,...,6=Saturday, empty = all)
     * @return PDF file as byte array with Content-Disposition attachment header
     * @throws IOException if PDF generation fails
     */
    @GetMapping("/{tutorId}/schedule/pdf")
    @PreAuthorize("(#tutorId == authentication.principal and hasRole('TUTOR')) or hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateSchedulePdf(
            @PathVariable Integer tutorId,
            @RequestParam(required = false, defaultValue = "") List<Integer> subjectIds,
            @RequestParam(required = false, defaultValue = "false") Boolean includeStudents,
            @RequestParam(required = false, defaultValue = "false") Boolean includeTotalHours,
            @RequestParam(required = false, defaultValue = "") List<Integer> days)
            throws IOException {

        // Tutor's offers, optionally filtered by subject
        List<Integer> tutorOfferIds = offerRepository.findAll().stream()
                .filter(o -> o.getTutorId().equals(tutorId))
                .filter(o -> subjectIds == null || subjectIds.isEmpty()
                        || subjectIds.contains(o.getSubjectId()))
                .map(o -> o.getId())
                .collect(Collectors.toList());

        // Active bookings (ACCEPTED = 6, PENDING = 3)
        var activeBookings = bookingRepository.findAll().stream()
                .filter(b -> tutorOfferIds.contains(b.getOfferId()))
                .filter(b -> b.getStatusId() == 6 || b.getStatusId() == 3)
                .collect(Collectors.toList());

        // Build schedule entries
        List<ScheduleEntry> entries = new ArrayList<>();

        for (var booking : activeBookings) {
            var slot = slotRepository.findById(booking.getAvailabilitySlotId())
                    .orElse(null);
            if (slot == null) continue;

            int dayOfWeek = slot.getDayOfWeek().intValue();

            if (days != null && !days.isEmpty() && !days.contains(dayOfWeek)) continue;

            int startHour = slot.getStartTime().getHour();

            if (startHour < 8 || startHour >= 17) continue;

            String subjectName = offerRepository.findById(booking.getOfferId())
                    .flatMap(o -> subjectRepository.findById(o.getSubjectId()))
                    .map(s -> s.getName())
                    .orElse("—");

            String studentName = null;
            if (Boolean.TRUE.equals(includeStudents)) {
                studentName = userRepository.findById(booking.getStudentId())
                        .map(u -> u.getFirstName() + " " + u.getLastName())
                        .orElse("—");
            }

            entries.add(new ScheduleEntry(dayOfWeek, startHour, subjectName, studentName));
        }

        // Tutor name for title
        String tutorName = userRepository.findById(tutorId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Korepetytor #" + tutorId);

        // Build PDF
        byte[] pdfBytes = new EduLinkSchedulePdfReport()
                .title("Plan zajęć: " + tutorName)
                .includeStudentNames(Boolean.TRUE.equals(includeStudents))
                .includeTotalHours(Boolean.TRUE.equals(includeTotalHours))
                .visibleDays(days)
                .addEntries(entries)
                .build();

        String filename = "edulink_plan_zajec_" + tutorId
                + "_" + LocalDate.now() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}