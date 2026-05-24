/*
 * TutorReportController.java
 *
 * Version: 1.0
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.pdf.EduLinkTutorPdfReport;
import com.vectorpeaks.pdf.EduLinkTutorPdfReport.*;
import com.vectorpeaks.backend.repository.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller that generates a PDF report for a specific tutor.
 * Provides detailed statistics about bookings, students, subjects, and reviews.
 *
 * @version 1.0
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/tutor")
@CrossOrigin(origins = "*")
public class TutorReportController {

    private final BookingRepository  bookingRepository;
    private final OfferRepository    offerRepository;
    private final UserRepository     userRepository;
    private final SubjectRepository  subjectRepository;
    private final ReviewRepository   reviewRepository;

    /**
     * Constructs a new TutorReportController with all required repositories.
     *
     * @param bookingRepository  repository for bookings
     * @param offerRepository    repository for offers
     * @param userRepository     repository for users
     * @param subjectRepository  repository for subjects
     * @param reviewRepository   repository for reviews
     */
    public TutorReportController(BookingRepository bookingRepository,
                                 OfferRepository offerRepository,
                                 UserRepository userRepository,
                                 SubjectRepository subjectRepository,
                                 ReviewRepository reviewRepository) {
        this.bookingRepository = bookingRepository;
        this.offerRepository   = offerRepository;
        this.userRepository    = userRepository;
        this.subjectRepository = subjectRepository;
        this.reviewRepository  = reviewRepository;
    }

    /**
     * Generates a PDF report for a tutor.
     *
     * @param tutorId          the ID of the tutor
     * @param from             start date (YYYY-MM-DD), optional, defaults to 30 days before today
     * @param to               end date (YYYY-MM-DD), optional, defaults to today
     * @param includeStudents  whether to include the list of students
     * @param includeSubjects  whether to include the subjects table
     * @param subjectIds       optional list of subject IDs to filter (empty = all)
     * @param includeReviews   whether to include student reviews
     * @param reviewsN         number of latest reviews to include (1-100, default 5)
     * @return PDF file as byte array with Content-Disposition attachment header
     * @throws IOException if PDF generation fails
     */
    @GetMapping("/{tutorId}/reports/pdf")
    public ResponseEntity<byte[]> generateTutorPdfReport(
            @PathVariable Integer tutorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "true")  Boolean includeStudents,
            @RequestParam(required = false, defaultValue = "true")  Boolean includeSubjects,
            @RequestParam(required = false, defaultValue = "")      List<Integer> subjectIds,
            @RequestParam(required = false, defaultValue = "true")  Boolean includeReviews,
            @RequestParam(required = false, defaultValue = "5")     Integer reviewsN)
            throws IOException {

        if (to   == null) to   = LocalDate.now();
        if (from == null) from = to.minusDays(30);

        LocalDateTime dtFrom = from.atStartOfDay();
        LocalDateTime dtTo   = to.atTime(23, 59, 59);

        reviewsN = Math.max(1, Math.min(100, reviewsN));

        String periodFrom = from.toString();
        String periodTo   = to.toString();

        // Get all offer IDs belonging to the tutor
        List<Integer> tutorOfferIds = offerRepository.findAll().stream()
                .filter(o -> o.getTutorId().equals(tutorId))
                .map(o -> o.getId())
                .collect(Collectors.toList());

        // Tutor bookings in the period
        var tutorBookings = bookingRepository.findAll().stream()
                .filter(b -> tutorOfferIds.contains(b.getOfferId()))
                .filter(b -> inPeriod(b.getBookingDate(), dtFrom, dtTo))
                .filter(b -> {
                    if (subjectIds == null || subjectIds.isEmpty()) return true;
                    Integer sid = offerRepository.findById(b.getOfferId())
                            .map(o -> o.getSubjectId()).orElse(-1);
                    return subjectIds.contains(sid);
                })
                .collect(Collectors.toList());

        // 1. Summary data
        long activeBookings = tutorBookings.stream()
                .filter(b -> b.getStatusId() == 6 || b.getStatusId() == 3)
                .count();

        long completedBookings = tutorBookings.stream()
                .filter(b -> b.getStatusId() == 4)
                .count();

        Double avgRating = reviewRepository.getAverageRatingByTutorId(tutorId);

        SummaryData summaryData = new SummaryData(
                activeBookings, completedBookings, avgRating);

        // 2. Students list
        List<StudentRow> studentRows = Collections.emptyList();
        if (Boolean.TRUE.equals(includeStudents)) {
            studentRows = tutorBookings.stream()
                    .map(b -> {
                        String studentName = userRepository.findById(b.getStudentId())
                                .map(u -> u.getFirstName() + " " + u.getLastName())
                                .orElse("—");
                        String subjectName = offerRepository.findById(b.getOfferId())
                                .flatMap(o -> subjectRepository.findById(o.getSubjectId()))
                                .map(s -> s.getName())
                                .orElse("—");
                        String status = statusNameById(b.getStatusId());
                        return new StudentRow(studentName, subjectName, status);
                    })
                    .collect(Collectors.toList());
        }

        // 3. Subjects table
        List<SubjectRow> subjectRows = Collections.emptyList();
        if (Boolean.TRUE.equals(includeSubjects)) {
            Map<Integer, Long> countPerSubject = tutorBookings.stream()
                    .collect(Collectors.groupingBy(
                            b -> offerRepository.findById(b.getOfferId())
                                    .map(o -> o.getSubjectId()).orElse(-1),
                            Collectors.counting()
                    ));

            subjectRows = countPerSubject.entrySet().stream()
                    .filter(e -> e.getKey() != -1)
                    .filter(e -> subjectIds == null || subjectIds.isEmpty()
                            || subjectIds.contains(e.getKey()))
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .map(e -> {
                        String name = subjectRepository.findById(e.getKey())
                                .map(s -> s.getName()).orElse("—");
                        Double avg = reviewRepository.getAverageRatingByTutorId(tutorId);
                        return new SubjectRow(name, e.getValue(), avg);
                    })
                    .collect(Collectors.toList());
        }

        // 4. Reviews
        List<ReviewRow> reviewRows = Collections.emptyList();
        if (Boolean.TRUE.equals(includeReviews)) {
            reviewRows = reviewRepository.findAll().stream()
                    .filter(r -> r.getTutorId().equals(tutorId))
                    .sorted(Comparator.comparing(r -> r.getCreatedAt(),
                            Comparator.reverseOrder()))
                    .limit(reviewsN)
                    .map(r -> {
                        String studentName = bookingRepository.findById(
                                        (int)(long) r.getBookingId())
                                .flatMap(b -> userRepository.findById(b.getStudentId()))
                                .map(u -> u.getFirstName() + " " + u.getLastName())
                                .orElse("—");
                        return new ReviewRow(
                                studentName,
                                r.getRating(),
                                r.getComment(),
                                r.getCreatedAt());
                    })
                    .collect(Collectors.toList());
        }

        // 5. Build PDF
        String tutorName = userRepository.findById(tutorId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Korepetytor #" + tutorId);

        EduLinkTutorPdfReport report = new EduLinkTutorPdfReport()
                .title("Raport korepetytora: " + tutorName)
                .period(periodFrom, periodTo)
                .addSummarySection(summaryData);

        if (Boolean.TRUE.equals(includeStudents) && !studentRows.isEmpty()) {
            report.addStudentsSection(studentRows);
        }
        if (Boolean.TRUE.equals(includeSubjects) && !subjectRows.isEmpty()) {
            report.addSubjectsSection(subjectRows);
        }
        if (Boolean.TRUE.equals(includeReviews) && !reviewRows.isEmpty()) {
            report.addReviewsSection(reviewRows);
        }

        byte[] pdfBytes = report.build();


        String filename = "edulink_raport_korepetytor_" + tutorId
                + "_" + periodFrom + "_" + periodTo + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Checks whether a LocalDateTime falls within the given inclusive period.
     *
     * @param dt   the date-time to check (may be null)
     * @param from period start (inclusive)
     * @param to   period end (inclusive)
     * @return true if dt is not null and within [from, to]
     */
    private boolean inPeriod(LocalDateTime dt, LocalDateTime from, LocalDateTime to) {
        return dt != null && !dt.isBefore(from) && !dt.isAfter(to);
    }

    /**
     * Maps a status ID to a human-readable status name.
     *
     * @param statusId the numeric status code
     * @return status string
     */
    private String statusNameById(int statusId) {
        switch (statusId) {
            case 1: return "ACTIVE";
            case 3: return "PENDING";
            case 4: return "COMPLETED";
            case 5: return "CANCELLED";
            case 6: return "ACCEPTED";
            case 7: return "REJECTED";
            default: return "UNKNOWN";
        }
    }
}