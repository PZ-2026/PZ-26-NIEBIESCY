/*
 * EduLinkSchedulePdfReport.java
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
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PDF report generator for tutor/student schedules.
 * Produces a weekly timetable (rows = hours 8–17, columns = filtered weekdays)
 * using a fluent Builder API.
 *
 * <p>Usage example:
 * <pre>
 * byte[] pdf = new EduLinkSchedulePdfReport()
 *     .title("Schedule: Jan Nowak")
 *     .includeStudentNames(true)
 *     .includeTotalHours(true)
 *     .addEntries(entries)
 *     .build();
 * </pre>
 *
 * @version 1.0.0
 * @author EduLink Team
 */
public class EduLinkSchedulePdfReport {

    // Colors
    private static final DeviceRgb COLOR_PRIMARY        = new DeviceRgb(63,  81,  181);
    private static final DeviceRgb COLOR_PRIMARY_LIGHT  = new DeviceRgb(197, 202, 233);
    private static final DeviceRgb COLOR_SURFACE        = new DeviceRgb(245, 245, 250);
    private static final DeviceRgb COLOR_TEXT_SECONDARY = new DeviceRgb(117, 117, 117);
    private static final DeviceRgb COLOR_CELL_FILLED    = new DeviceRgb(232, 234, 246);

    // Constants
    /** Table rows cover hours 08:00 – 17:00 (10 rows). */
    private static final int HOUR_FROM = 8;
    private static final int HOUR_TO   = 18;

    /**
     * Day names. dayOfWeek mapping: 0 = Sunday, 1 = Monday, …, 6 = Saturday
     * (consistent with the database).
     */
    private static final String[] DAY_NAMES = {
            "Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"
    };

    // Builder state
    private String            reportTitle        = "Schedule";
    private boolean           includeStudentNames = false;
    private boolean           includeTotalHours   = false;
    /** null = all days; non‑empty list = only these days (values 0–6). */
    private List<Integer>     visibleDays        = null;
    private List<ScheduleEntry> entries          = Collections.emptyList();

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
    public EduLinkSchedulePdfReport title(String title) {
        this.reportTitle = title;
        return this;
    }

    /**
     * Sets whether student names should be included in the schedule cells.
     *
     * @param value true to show student names, false to hide them
     * @return this instance for method chaining
     */
    public EduLinkSchedulePdfReport includeStudentNames(boolean value) {
        this.includeStudentNames = value;
        return this;
    }

    /**
     * Sets whether the total number of hours should be displayed under the table.
     *
     * @param value true to show total hours, false to hide
     * @return this instance for method chaining
     */
    public EduLinkSchedulePdfReport includeTotalHours(boolean value) {
        this.includeTotalHours = value;
        return this;
    }

    /**
     * Filters the table columns to the given weekdays.
     * Pass null or an empty list to show all days.
     *
     * @param days list of day values 0–6 (0 = Sunday, 1 = Monday, …, 6 = Saturday)
     * @return this instance for method chaining
     */
    public EduLinkSchedulePdfReport visibleDays(List<Integer> days) {
        this.visibleDays = (days == null || days.isEmpty()) ? null : new ArrayList<>(days);
        return this;
    }

    /**
     * Adds schedule entries to be rendered in the PDF.
     *
     * @param entries list of schedule entries
     * @return this instance for method chaining
     */
    public EduLinkSchedulePdfReport addEntries(List<ScheduleEntry> entries) {
        this.entries = entries != null ? entries : Collections.emptyList();
        return this;
    }

    // ========================================================================
    // Build
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
        Document    document = new Document(pdfDoc, PageSize.A4.rotate()); // landscape
        document.setMargins(30, 40, 30, 40);

        renderHeader(document);
        renderMetaInfo(document);
        renderDivider(document);
        renderScheduleTable(document);
        if (includeTotalHours) renderTotalHours(document);
        renderFooter(document);

        document.close();
        return out.toByteArray();
    }

    // ========================================================================
    // Rendering sections
    // ========================================================================

    private void renderHeader(Document doc) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth();
        Cell cell = new Cell()
                .setBackgroundColor(COLOR_PRIMARY)
                .setPadding(16).setBorder(Border.NO_BORDER);
        cell.add(new Paragraph("EduLink")
                .setFont(fontBold).setFontSize(10)
                .setFontColor(COLOR_PRIMARY_LIGHT).setMarginBottom(3));
        cell.add(new Paragraph(reportTitle)
                .setFont(fontBold).setFontSize(18)
                .setFontColor(ColorConstants.WHITE).setMarginBottom(0));
        t.addCell(cell);
        doc.add(t);
        doc.add(new Paragraph().setMarginBottom(8));
    }

    private void renderMetaInfo(Document doc) {
        String generated = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        Table meta = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();
        meta.addCell(metaCell("Tutor schedule", "", TextAlignment.LEFT));
        meta.addCell(metaCell("Generated on:", generated, TextAlignment.RIGHT));
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
        doc.add(new Paragraph().setMarginBottom(12));
    }

    private void renderScheduleTable(Document doc) {
        List<Integer> days = buildVisibleDays();
        Map<Integer, Map<Integer, List<ScheduleEntry>>> index = buildIndex();

        float[] colWidths = new float[days.size() + 1];
        colWidths[0] = 1.2f;
        for (int i = 1; i <= days.size(); i++) colWidths[i] = 2f;

        Table table = new Table(UnitValue.createPercentArray(colWidths))
                .useAllAvailableWidth()
                .setMarginBottom(8);

        // Header row
        table.addHeaderCell(headerCell("Time"));
        for (int day : days) {
            table.addHeaderCell(headerCell(DAY_NAMES[day]));
        }

        // Hour rows
        for (int hour = HOUR_FROM; hour < HOUR_TO; hour++) {
            boolean oddRow = ((hour - HOUR_FROM) % 2 == 0);
            DeviceRgb rowBg = oddRow ? COLOR_SURFACE : null;

            String timeLabel = String.format("%02d:00", hour);
            table.addCell(timeCell(timeLabel, rowBg));

            for (int day : days) {
                List<ScheduleEntry> cellEntries = index
                        .getOrDefault(day, Collections.emptyMap())
                        .getOrDefault(hour, Collections.emptyList());
                table.addCell(scheduleCell(cellEntries, rowBg));
            }
        }

        doc.add(table);
    }

    private void renderTotalHours(Document doc) {
        long total = entries.size();
        doc.add(new Paragraph("Total hours: " + total)
                .setFont(fontBold).setFontSize(11)
                .setFontColor(COLOR_PRIMARY)
                .setMarginTop(4).setMarginBottom(16));
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
    // Helper methods
    // ========================================================================

    /**
     * Builds the list of days to display.
     * Default (when visibleDays is null) returns Monday through Sunday (1–6, 0).
     */
    private List<Integer> buildVisibleDays() {
        if (visibleDays != null && !visibleDays.isEmpty()) {
            return visibleDays.stream().sorted().collect(Collectors.toList());
        }
        return Arrays.asList(1, 2, 3, 4, 5, 6, 0);
    }

    /**
     * Builds an index mapping dayOfWeek → startHour → list of entries.
     */
    private Map<Integer, Map<Integer, List<ScheduleEntry>>> buildIndex() {
        Map<Integer, Map<Integer, List<ScheduleEntry>>> index = new HashMap<>();
        for (ScheduleEntry e : entries) {
            index
                    .computeIfAbsent(e.dayOfWeek, k -> new HashMap<>())
                    .computeIfAbsent(e.startHour, k -> new ArrayList<>())
                    .add(e);
        }
        return index;
    }

    private Cell headerCell(String text) {
        return new Cell()
                .setBackgroundColor(COLOR_PRIMARY)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY_LIGHT, 1f))
                .setPadding(6)
                .add(new Paragraph(text)
                        .setFont(fontBold).setFontSize(8)
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER));
    }

    private Cell timeCell(String text, DeviceRgb bg) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY_LIGHT, 0.4f))
                .setPadding(5)
                .setTextAlignment(TextAlignment.CENTER);
        if (bg != null) cell.setBackgroundColor(bg);
        cell.add(new Paragraph(text)
                .setFont(fontBold).setFontSize(8)
                .setFontColor(COLOR_TEXT_SECONDARY));
        return cell;
    }

    private Cell scheduleCell(List<ScheduleEntry> cellEntries, DeviceRgb defaultBg) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY_LIGHT, 0.4f))
                .setPadding(4);

        if (cellEntries.isEmpty()) {
            if (defaultBg != null) cell.setBackgroundColor(defaultBg);
            cell.add(new Paragraph("").setFontSize(7));
            return cell;
        }

        cell.setBackgroundColor(COLOR_CELL_FILLED);
        for (ScheduleEntry e : cellEntries) {
            cell.add(new Paragraph(e.subjectName)
                    .setFont(fontBold).setFontSize(7.5f)
                    .setFontColor(COLOR_PRIMARY)
                    .setMarginBottom(includeStudentNames ? 1 : 0));

            if (includeStudentNames && e.studentName != null) {
                cell.add(new Paragraph(e.studentName)
                        .setFont(fontRegular).setFontSize(7)
                        .setFontColor(COLOR_TEXT_SECONDARY)
                        .setMarginBottom(2));
            }
        }
        return cell;
    }

    private Cell metaCell(String label, String value, TextAlignment align) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(align);
        if (!label.isEmpty()) {
            cell.add(new Paragraph(label)
                    .setFont(fontBold).setFontSize(8)
                    .setFontColor(COLOR_TEXT_SECONDARY));
        }
        if (!value.isEmpty()) {
            cell.add(new Paragraph(value)
                    .setFont(fontRegular).setFontSize(9));
        }
        return cell;
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
    // Data Transfer Object
    // ========================================================================

    /**
     * A single entry in the schedule – one slot of one booking.
     */
    public static class ScheduleEntry {
        /** Day of week: 0=Sunday, 1=Monday, …, 6=Saturday. */
        public final int    dayOfWeek;
        /** Start hour (8–17). */
        public final int    startHour;
        /** Name of the subject. */
        public final String subjectName;
        /** Full name of the student (may be null if not displayed). */
        public final String studentName;

        /**
         * Constructs a new ScheduleEntry.
         *
         * @param dayOfWeek   the day of week (0–6)
         * @param startHour   the start hour (8–17)
         * @param subjectName the subject name
         * @param studentName the student name (nullable)
         */
        public ScheduleEntry(int dayOfWeek, int startHour,
                             String subjectName, String studentName) {
            this.dayOfWeek   = dayOfWeek;
            this.startHour   = startHour;
            this.subjectName = subjectName;
            this.studentName = studentName;
        }
    }
}