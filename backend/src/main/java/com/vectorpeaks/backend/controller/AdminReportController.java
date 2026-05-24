/*
 * AdminReportController.java
 *
 * Version: 1.0
 * Date: 2026-05-17
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

// Import from external library edulink-pdf-1.0.0.jar (located in libs/)
import com.vectorpeaks.pdf.EduLinkPdfReport;
import com.vectorpeaks.pdf.EduLinkPdfReport.*;
import com.vectorpeaks.backend.repository.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller responsible for generating PDF reports for the administrator panel.
 * Uses the custom EduLinkPdfReport library (based on iText 9) to produce statistical reports.
 *
 * @version 1.0
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class AdminReportController {

    private final BookingRepository  bookingRepository;
    private final OfferRepository    offerRepository;
    private final UserRepository     userRepository;
    private final SubjectRepository  subjectRepository;
    private final ReviewRepository   reviewRepository;

    /**
     * Constructs a new AdminReportController with all required repositories.
     *
     * @param bookingRepository  repository for bookings
     * @param offerRepository    repository for offers
     * @param userRepository     repository for users
     * @param subjectRepository  repository for subjects
     * @param reviewRepository   repository for reviews
     */
    public AdminReportController(BookingRepository bookingRepository,
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
     * Generates a PDF report for the given time period.
     * Default period: last 30 days.
     *
     * @param from start date (YYYY-MM-DD), optional, defaults to 30 days before today
     * @param to   end date (YYYY-MM-DD), optional, defaults to today
     * @return PDF file as byte array with Content-Disposition attachment header
     * @throws IOException if PDF generation fails
     */
    @GetMapping("/reports/pdf")
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to)
            throws IOException {

        if (to   == null) to   = LocalDate.now();
        if (from == null) from = to.minusDays(30);

        LocalDateTime dtFrom = from.atStartOfDay();
        LocalDateTime dtTo   = to.atTime(23, 59, 59);

        String periodFrom = from.toString();
        String periodTo   = to.toString();

        // 1. Summary data
        long totalBookings = bookingRepository.findAll().stream()
                .filter(b -> inPeriod(b.getBookingDate(), dtFrom, dtTo))
                .count();

        long newOffers = offerRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null
                        && !o.getCreatedAt().isBefore(dtFrom)
                        && !o.getCreatedAt().isAfter(dtTo))
                .count();

        long newUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null
                        && !u.getCreatedAt().isBefore(dtFrom)
                        && !u.getCreatedAt().isAfter(dtTo))
                .count();

        ReportSummaryData summary = new ReportSummaryData(
                totalBookings,
                newOffers,
                newUsers
        );

        // 2. Booking status distribution
        Map<String, Long> statusMap = new LinkedHashMap<>();
        statusMap.put("COMPLETED", bookingRepository.findAll().stream()
                .filter(b -> inPeriod(b.getBookingDate(), dtFrom, dtTo) && b.getStatusId() == 4).count());
        statusMap.put("ACCEPTED",  bookingRepository.findAll().stream()
                .filter(b -> inPeriod(b.getBookingDate(), dtFrom, dtTo) && b.getStatusId() == 6).count());
        statusMap.put("PENDING",   bookingRepository.findAll().stream()
                .filter(b -> inPeriod(b.getBookingDate(), dtFrom, dtTo) && b.getStatusId() == 3).count());
        statusMap.put("REJECTED",  bookingRepository.findAll().stream()
                .filter(b -> inPeriod(b.getBookingDate(), dtFrom, dtTo) && b.getStatusId() == 7).count());

        // 3. Top subjects
        Map<Integer, Long> bookingsPerSubject = bookingRepository.findAll().stream()
                .filter(b -> inPeriod(b.getBookingDate(), dtFrom, dtTo))
                .collect(Collectors.groupingBy(
                        b -> {
                            var offer = offerRepository.findById(b.getOfferId()).orElse(null);
                            return offer != null ? offer.getSubjectId() : -1;
                        },
                        Collectors.counting()
                ));

        List<SubjectReportRow> topSubjects = bookingsPerSubject.entrySet().stream()
                .filter(e -> e.getKey() != -1)
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    String name = subjectRepository.findById(e.getKey())
                            .map(s -> s.getName()).orElse("—");
                    Double avg = reviewRepository.getAverageRatingByTutorId(e.getKey());
                    return new SubjectReportRow(name, e.getValue(), avg);
                })
                .collect(Collectors.toList());

        // 4. Top tutors
        Map<Integer, Long> bookingsPerTutor = bookingRepository.findAll().stream()
                .filter(b -> inPeriod(b.getBookingDate(), dtFrom, dtTo))
                .collect(Collectors.groupingBy(
                        b -> {
                            var offer = offerRepository.findById(b.getOfferId()).orElse(null);
                            return offer != null ? offer.getTutorId() : -1;
                        },
                        Collectors.counting()
                ));

        List<TutorReportRow> topTutors = bookingsPerTutor.entrySet().stream()
                .filter(e -> e.getKey() != -1)
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Integer tutorId = e.getKey();
                    String name = userRepository.findById(tutorId)
                            .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("—");
                    Double avg = reviewRepository.getAverageRatingByTutorId(tutorId);
                    return new TutorReportRow(name, e.getValue(), avg);
                })
                .collect(Collectors.toList());

        // 5. Build PDF using the custom library
        byte[] pdfBytes = new EduLinkPdfReport()
                .title("Raport statystyk platformy")
                .period(periodFrom, periodTo)
                .addSummarySection(summary)
                .addBookingStatusSection(statusMap)
                .addTopSubjectsSection(topSubjects)
                .addTopTutorsSection(topTutors)
                .build();

        // 6. Return as downloadable file
        String filename = "edulink_raport_" + periodFrom + "_" + periodTo + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
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
}