/*
 * StudentScheduleController.java
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for generating PDF schedules for students.
 * Provides an endpoint to export a student's lesson timetable as a PDF file.
 *
 * @version 1.0
 * @author EduLink Team
 */

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentScheduleController {

    private final BookingRepository bookingRepository;
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final AvailabilitySlotRepository slotRepository;

    /**
     * Constructs a new StudentScheduleController with required repositories.
     *
     * @param bookingRepository repository for bookings
     * @param offerRepository   repository for offers
     * @param userRepository    repository for users
     * @param subjectRepository repository for subjects
     * @param slotRepository    repository for availability slots
     */
    public StudentScheduleController(BookingRepository bookingRepository,
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
     * Generates a PDF schedule for a given student based on active bookings.
     * Allows filtering by subject IDs, days of week, and optional inclusion of tutor names and total hours.
     *
     * @param studentId       the ID of the student
     * @param subjectIds      optional list of subject IDs to filter (empty = all subjects)
     * @param includeTutors   if true, include tutor names in the schedule
     * @param includeTotalHours if true, include total hours summary
     * @param days            optional list of days of week to filter (0=Sunday,...,6=Saturday)
     * @return PDF file as byte array with Content-Disposition attachment header
     * @throws IOException if PDF generation fails
     */
    @GetMapping("/{studentId}/schedule/pdf")
    public ResponseEntity<byte[]> generateSchedulePdf(
            @PathVariable Integer studentId,
            @RequestParam(required = false, defaultValue = "") List<Integer> subjectIds,
            @RequestParam(required = false, defaultValue = "false") Boolean includeTutors,
            @RequestParam(required = false, defaultValue = "false") Boolean includeTotalHours,
            @RequestParam(required = false, defaultValue = "") List<Integer> days)
            throws IOException {

        // Only accepted bookings
        var activeBookings = bookingRepository.findByStudentId(studentId).stream()
                .filter(b -> b.getStatusId() == 6)
                .collect(Collectors.toList());

        List<ScheduleEntry> entries = new ArrayList<>();

        for (var booking : activeBookings) {
            var offer = offerRepository.findById(booking.getOfferId()).orElse(null);
            if (offer == null) continue;

            // Filter by subject if requested
            if (subjectIds != null && !subjectIds.isEmpty()
                    && !subjectIds.contains(offer.getSubjectId())) continue;

            var slot = slotRepository.findById(booking.getAvailabilitySlotId()).orElse(null);
            if (slot == null) continue;

            int dayOfWeek = slot.getDayOfWeek().intValue();
            if (days != null && !days.isEmpty() && !days.contains(dayOfWeek)) continue;

            int startHour = slot.getStartTime().getHour();
            if (startHour < 8 || startHour >= 17) continue;

            String subjectName = subjectRepository.findById(offer.getSubjectId())
                    .map(s -> s.getName()).orElse("—");

            String tutorName = null;
            if (Boolean.TRUE.equals(includeTutors)) {
                tutorName = userRepository.findById(offer.getTutorId())
                        .map(u -> u.getFirstName() + " " + u.getLastName())
                        .orElse("—");
            }

            entries.add(new ScheduleEntry(dayOfWeek, startHour, subjectName, tutorName));
        }

        String studentName = userRepository.findById(studentId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Uczeń #" + studentId);

        byte[] pdfBytes = new EduLinkSchedulePdfReport()
                .title("Plan zajęć: " + studentName)
                .includeStudentNames(Boolean.TRUE.equals(includeTutors))
                .includeTotalHours(Boolean.TRUE.equals(includeTotalHours))
                .visibleDays(days)
                .addEntries(entries)
                .build();

        String filename = "edulink_plan_zajec_uczen_" + studentId
                + "_" + LocalDate.now() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}