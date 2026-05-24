/*
 * EduLinkTutorPdfReport.java
 *
 * Version: 1.0.0
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.pdf;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF report generator for a tutor.
 * Provides a fluent Builder API to produce a report containing
 * summary statistics, student lists, subject breakdowns, and reviews.
 *
 * <p>Usage example:
 * <pre>
 * byte[] pdf = new EduLinkTutorPdfReport()
 *     .title("Tutor report: Jan Nowak")
 *     .period("2026-01-01", "2026-12-31")
 *     .addSummarySection(summaryData)
 *     .addStudentsSection(studentList)
 *     .addSubjectsSection(subjectList)
 *     .addReviewsSection(reviewList)
 *     .build();
 * </pre>
 *
 * @version 1.0.0
 * @author EduLink Team
 */
public class EduLinkTutorPdfReport {

    // Brand colors (identical to EduLinkPdfReport)
    private static final DeviceRgb COLOR_PRIMARY        = new DeviceRgb(63,  81,  181);
    private static final DeviceRgb COLOR_PRIMARY_LIGHT  = new DeviceRgb(197, 202, 233);
    private static final DeviceRgb COLOR_SUCCESS        = new DeviceRgb(76,  175,  80);
    private static final DeviceRgb COLOR_TEAL           = new DeviceRgb(  0, 150, 136);
    private static final DeviceRgb COLOR_SURFACE        = new DeviceRgb(245, 245, 250);
    private static final DeviceRgb COLOR_TEXT_SECONDARY = new DeviceRgb(117, 117, 117);

    // Builder state
    private String              reportTitle  = "Tutor report";
    private String              periodFrom   = "";
    private String              periodTo     = "";
    private SummaryData         summaryData  = null;
    private List<StudentRow>    students     = null;
    private List<SubjectRow>    subjects     = null;
    private List<ReviewRow>     reviews      = null;

    // Fonts (initialized in build())
    private PdfFont fontRegular;
    private PdfFont fontBold;

    // ========================================================================
    // Builder API
    // ========================================================================

    /**
     * Sets the report title displayed in the PDF header.
     *
     * @param title the report title
     * @return this instance for method chaining
     */
    public EduLinkTutorPdfReport title(String title) {
        this.reportTitle = title;
        return this;
    }

    /**
     * Sets the date range of the report (displayed in meta information).
     *
     * @param from start date (e.g., "2026-01-01")
     * @param to   end date (e.g., "2026-12-31")
     * @return this instance for method chaining
     */
    public EduLinkTutorPdfReport period(String from, String to) {
        this.periodFrom = from;
        this.periodTo   = to;
        return this;
    }

    /**
     * Adds the summary statistics section.
     *
     * @param data the summary data object
     * @return this instance for method chaining
     */
    public EduLinkTutorPdfReport addSummarySection(SummaryData data) {
        this.summaryData = data;
        return this;
    }

    /**
     * Adds the list of students (with subject and status) as a table.
     *
     * @param studentList list of student rows
     * @return this instance for method chaining
     */
    public EduLinkTutorPdfReport addStudentsSection(List<StudentRow> studentList) {
        this.students = studentList;
        return this;
    }

    /**
     * Adds a table of subjects taught (with booking counts and average ratings).
     *
     * @param subjectList list of subject rows
     * @return this instance for method chaining
     */
    public EduLinkTutorPdfReport addSubjectsSection(List<SubjectRow> subjectList) {
        this.subjects = subjectList;
        return this;
    }

    /**
     * Adds a table of reviews left by students.
     *
     * @param reviewList list of review rows
     * @return this instance for method chaining
     */
    public EduLinkTutorPdfReport addReviewsSection(List<ReviewRow> reviewList) {
        this.reviews = reviewList;
        return this;
    }

    // ========================================================================
    // PDF building
    // ========================================================================

    /**
     * Builds the PDF report and returns its content as a byte array.
     *
     * @return byte array containing the generated PDF
     * @throws IOException if font loading or PDF writing fails
     */
    public byte[] build() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        fontRegular = loadFont("DejaVuSans.ttf");
        fontBold    = loadFont("DejaVuSans-Bold.ttf");

        PdfWriter   writer   = new PdfWriter(out);
        PdfDocument pdfDoc   = new PdfDocument(writer);
        Document    document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(40, 50, 40, 50);

        renderHeader(document);
        renderMetaInfo(document);
        renderDivider(document);

        if (summaryData != null) renderSummaryCards(document);
        if (students    != null) renderStudentsTable(document);
        if (subjects    != null) renderSubjectsTable(document);
        if (reviews     != null) renderReviewsTable(document);

        renderFooter(document);
        document.close();

        return out.toByteArray();
    }

    // ========================================================================
    // Rendering sections
    // ========================================================================

    private void renderHeader(Document doc) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth();

        Cell cell = new Cell()
                .setBackgroundColor(COLOR_PRIMARY)
                .setPadding(20)
                .setBorder(Border.NO_BORDER);

        cell.add(new Paragraph("EduLink")
                .setFont(fontBold).setFontSize(11)
                .setFontColor(COLOR_PRIMARY_LIGHT).setMarginBottom(4));

        cell.add(new Paragraph(reportTitle)
                .setFont(fontBold).setFontSize(22)
                .setFontColor(ColorConstants.WHITE).setMarginBottom(0));

        headerTable.addCell(cell);
        doc.add(headerTable);
        doc.add(new Paragraph().setMarginBottom(12));
    }

    private void renderMetaInfo(Document doc) {
        String generated = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        Table meta = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();

        meta.addCell(metaCell("Report period:",
                periodFrom + " — " + periodTo, TextAlignment.LEFT));
        meta.addCell(metaCell("Generated on:",
                generated, TextAlignment.RIGHT));

        doc.add(meta);
    }

    private void renderDivider(Document doc) {
        doc.add(new Paragraph().setMarginTop(4));
        Table line = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY_LIGHT, 1.5f))
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER);
        line.addCell(new Cell().setBorder(Border.NO_BORDER).setHeight(1));
        doc.add(line);
        doc.add(new Paragraph().setMarginBottom(16));
    }

    private void renderSummaryCards(Document doc) {
        addSectionTitle(doc, "Summary");

        Table cards = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        cards.addCell(summaryCard(
                "Active\ntutorings",
                String.valueOf(summaryData.activeBookings),
                COLOR_PRIMARY));
        cards.addCell(summaryCard(
                "Completed\ntutorings",
                String.valueOf(summaryData.completedBookings),
                COLOR_SUCCESS));
        cards.addCell(summaryCard(
                "Average rating",
                summaryData.avgRating != null
                        ? String.format("%.1f ★", summaryData.avgRating)
                        : "—",
                COLOR_TEAL));

        doc.add(cards);
    }

    private void renderStudentsTable(Document doc) {
        if (students.isEmpty()) return;
        addSectionTitle(doc, "Students");

        Table table = styledTable(new float[]{0.5f, 3, 2, 1.5f});
        table.addHeaderCell(headerCell("#"));
        table.addHeaderCell(headerCell("Student"));
        table.addHeaderCell(headerCell("Subject"));
        table.addHeaderCell(headerCell("Status"));

        for (int i = 0; i < students.size(); i++) {
            StudentRow row = students.get(i);
            DeviceRgb bg = (i % 2 == 0) ? COLOR_SURFACE : null;
            table.addCell(dataCell(String.valueOf(i + 1),          bg, TextAlignment.CENTER));
            table.addCell(dataCell(row.studentName,                bg, TextAlignment.LEFT));
            table.addCell(dataCell(row.subjectName,                bg, TextAlignment.LEFT));
            table.addCell(dataCell(translateStatus(row.bookingStatus), bg, TextAlignment.CENTER));
        }

        doc.add(table);
        doc.add(new Paragraph().setMarginBottom(20));
    }

    private void renderSubjectsTable(Document doc) {
        if (subjects.isEmpty()) return;
        addSectionTitle(doc, "Subjects taught");

        Table table = styledTable(new float[]{0.5f, 3, 1.5f, 1.5f});
        table.addHeaderCell(headerCell("#"));
        table.addHeaderCell(headerCell("Subject"));
        table.addHeaderCell(headerCell("Bookings"));
        table.addHeaderCell(headerCell("Avg rating"));

        for (int i = 0; i < subjects.size(); i++) {
            SubjectRow row = subjects.get(i);
            DeviceRgb bg = (i % 2 == 0) ? COLOR_SURFACE : null;
            table.addCell(dataCell(String.valueOf(i + 1),            bg, TextAlignment.CENTER));
            table.addCell(dataCell(row.subjectName,                  bg, TextAlignment.LEFT));
            table.addCell(dataCell(String.valueOf(row.bookingCount), bg, TextAlignment.CENTER));
            table.addCell(dataCell(
                    row.avgRating != null ? String.format("%.1f ★", row.avgRating) : "—",
                    bg, TextAlignment.CENTER));
        }

        doc.add(table);
        doc.add(new Paragraph().setMarginBottom(20));
    }

    private void renderReviewsTable(Document doc) {
        if (reviews.isEmpty()) return;
        addSectionTitle(doc, "Student reviews");

        Table table = styledTable(new float[]{2, 0.7f, 4, 1.5f});
        table.addHeaderCell(headerCell("Student"));
        table.addHeaderCell(headerCell("Rating"));
        table.addHeaderCell(headerCell("Comment"));
        table.addHeaderCell(headerCell("Date"));

        for (int i = 0; i < reviews.size(); i++) {
            ReviewRow row = reviews.get(i);
            DeviceRgb bg = (i % 2 == 0) ? COLOR_SURFACE : null;
            table.addCell(dataCell(row.studentName,                       bg, TextAlignment.LEFT));
            table.addCell(dataCell(row.rating + " ★",                    bg, TextAlignment.CENTER));
            table.addCell(dataCell(row.comment != null ? row.comment : "—", bg, TextAlignment.LEFT));
            table.addCell(dataCell(
                    row.createdAt != null
                            ? row.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                            : "—",
                    bg, TextAlignment.CENTER));
        }

        doc.add(table);
        doc.add(new Paragraph().setMarginBottom(20));
    }

    private void renderFooter(Document doc) {
        doc.add(new Paragraph().setMarginTop(8));
        Table line = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY_LIGHT, 1f))
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER);
        line.addCell(new Cell().setBorder(Border.NO_BORDER).setHeight(1));
        doc.add(line);
        doc.add(new Paragraph("EduLink © 2026 | Report automatically generated by the system")
                .setFont(fontRegular).setFontSize(8)
                .setFontColor(COLOR_TEXT_SECONDARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(6));
    }

    // ========================================================================
    // Helper methods for creating PDF elements
    // ========================================================================

    private void addSectionTitle(Document doc, String title) {
        doc.add(new Paragraph(title)
                .setFont(fontBold).setFontSize(13)
                .setFontColor(COLOR_PRIMARY).setMarginBottom(8));
    }

    private Cell metaCell(String label, String value, TextAlignment align) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(align);
        cell.add(new Paragraph(label)
                .setFont(fontBold).setFontSize(8).setFontColor(COLOR_TEXT_SECONDARY));
        cell.add(new Paragraph(value)
                .setFont(fontRegular).setFontSize(10));
        return cell;
    }

    private Cell summaryCard(String label, String value, DeviceRgb color) {
        Cell card = new Cell()
                .setBackgroundColor(COLOR_SURFACE)
                .setBorder(new SolidBorder(color, 2))
                .setPadding(12).setMargin(4)
                .setTextAlignment(TextAlignment.CENTER);
        card.add(new Paragraph(value)
                .setFont(fontBold).setFontSize(20)
                .setFontColor(color).setMarginBottom(4));
        card.add(new Paragraph(label)
                .setFont(fontRegular).setFontSize(9)
                .setFontColor(COLOR_TEXT_SECONDARY));
        return card;
    }

    private Table styledTable(float[] cols) {
        return new Table(UnitValue.createPercentArray(cols))
                .useAllAvailableWidth().setMarginBottom(4);
    }

    private Cell headerCell(String text) {
        return new Cell()
                .setBackgroundColor(COLOR_PRIMARY)
                .setBorder(Border.NO_BORDER).setPadding(8)
                .add(new Paragraph(text)
                        .setFont(fontBold).setFontSize(9)
                        .setFontColor(ColorConstants.WHITE));
    }

    private Cell dataCell(String text, DeviceRgb bg, TextAlignment align) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY_LIGHT, 0.5f))
                .setPadding(7).setTextAlignment(align);
        if (bg != null) cell.setBackgroundColor(bg);
        cell.add(new Paragraph(text).setFont(fontRegular).setFontSize(9));
        return cell;
    }

    private String translateStatus(String status) {
        switch (status.toUpperCase()) {
            case "COMPLETED": return "Completed";
            case "ACCEPTED":  return "Accepted";
            case "PENDING":   return "Pending";
            case "REJECTED":  return "Rejected";
            case "CANCELLED": return "Cancelled";
            default:          return status;
        }
    }

    private PdfFont loadFont(String resourceName) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            return PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA,
                    PdfEncodings.WINANSI,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        }
        byte[] fontBytes = is.readAllBytes();
        is.close();
        return PdfFontFactory.createFont(
                fontBytes, PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
    }

    // ========================================================================
    // Data Transfer Objects (inner DTOs)
    // ========================================================================

    /**
     * Summary data for the cards section.
     */
    public static class SummaryData {
        /** Number of active bookings (accepted or pending). */
        public final long   activeBookings;
        /** Number of completed bookings. */
        public final long   completedBookings;
        /** Average rating of the tutor (may be null if no reviews). */
        public final Double avgRating;

        /**
         * Constructs a new SummaryData object.
         *
         * @param activeBookings    number of active bookings
         * @param completedBookings number of completed bookings
         * @param avgRating         average rating (nullable)
         */
        public SummaryData(long activeBookings, long completedBookings, Double avgRating) {
            this.activeBookings    = activeBookings;
            this.completedBookings = completedBookings;
            this.avgRating         = avgRating;
        }
    }

    /**
     * Row data for the students table.
     */
    public static class StudentRow {
        /** Full name of the student. */
        public final String studentName;
        /** Name of the subject. */
        public final String subjectName;
        /** Status of the booking (e.g., "ACCEPTED", "PENDING"). */
        public final String bookingStatus;

        /**
         * Constructs a new StudentRow object.
         *
         * @param studentName   student's full name
         * @param subjectName   subject name
         * @param bookingStatus booking status string
         */
        public StudentRow(String studentName, String subjectName, String bookingStatus) {
            this.studentName   = studentName;
            this.subjectName   = subjectName;
            this.bookingStatus = bookingStatus;
        }
    }

    /**
     * Row data for the subjects table.
     */
    public static class SubjectRow {
        /** Name of the subject. */
        public final String subjectName;
        /** Number of bookings for this subject. */
        public final long   bookingCount;
        /** Average rating (may be null if no reviews). */
        public final Double avgRating;

        /**
         * Constructs a new SubjectRow object.
         *
         * @param subjectName  subject name
         * @param bookingCount number of bookings
         * @param avgRating    average rating (nullable)
         */
        public SubjectRow(String subjectName, long bookingCount, Double avgRating) {
            this.subjectName  = subjectName;
            this.bookingCount = bookingCount;
            this.avgRating    = avgRating;
        }
    }

    /**
     * Row data for the reviews table.
     */
    public static class ReviewRow {
        /** Full name of the student who left the review. */
        public final String        studentName;
        /** Rating value (1-5). */
        public final int           rating;
        /** Comment text (may be null). */
        public final String        comment;
        /** Timestamp when the review was created. */
        public final LocalDateTime createdAt;

        /**
         * Constructs a new ReviewRow object.
         *
         * @param studentName student's full name
         * @param rating      rating value
         * @param comment     review comment (nullable)
         * @param createdAt   creation timestamp
         */
        public ReviewRow(String studentName, int rating,
                         String comment, LocalDateTime createdAt) {
            this.studentName = studentName;
            this.rating      = rating;
            this.comment     = comment;
            this.createdAt   = createdAt;
        }
    }
}