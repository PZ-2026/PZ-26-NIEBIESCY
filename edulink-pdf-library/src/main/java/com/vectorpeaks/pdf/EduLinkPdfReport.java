/*
 * EduLinkPdfReport.java
 *
 * Version: 1.0.0
 * Date: 2026-05-16
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * Własna biblioteka EduLink do generowania raportów PDF.
 * Korzysta z iText 9 (com.itextpdf:itext-core) jako silnika renderującego.
 *
 * Sposób użycia (wzorzec Builder):
 *
 *   byte[] pdf = new EduLinkPdfReport()
 *       .title("Raport EduLink")
 *       .period("2026-01-01", "2026-12-31")
 *       .addSummarySection(summaryData)
 *       .addBookingStatusSection(statusMap)
 *       .addTopSubjectsSection(subjectList)
 *       .addTopTutorsSection(tutorList)
 *       .build();
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
import java.util.Map;

/**
 * Główna klasa biblioteki EduLink PDF.
 *
 * <p>Udostępnia fluent API (Builder pattern) pozwalający skonfigurować
 * raport i zbudować go do tablicy bajtów gotowej do wysłania jako
 * odpowiedź HTTP lub zapisu do pliku.
 */
public class EduLinkPdfReport {

    // ── Kolory brandingowe EduLink ───────────────────────────────────────────
    private static final DeviceRgb COLOR_PRIMARY        = new DeviceRgb(63,  81,  181);
    private static final DeviceRgb COLOR_PRIMARY_LIGHT  = new DeviceRgb(197, 202, 233);
    private static final DeviceRgb COLOR_SUCCESS        = new DeviceRgb(76,  175,  80);
    private static final DeviceRgb COLOR_WARNING        = new DeviceRgb(255, 193,   7);
    private static final DeviceRgb COLOR_TEAL           = new DeviceRgb(  0, 150, 136);
    private static final DeviceRgb COLOR_SURFACE        = new DeviceRgb(245, 245, 250);
    private static final DeviceRgb COLOR_TEXT_SECONDARY = new DeviceRgb(117, 117, 117);

    // ── Stan buildera ────────────────────────────────────────────────────────
    private String               reportTitle      = "Raport EduLink";
    private String               periodFrom       = "";
    private String               periodTo         = "";
    private ReportSummaryData    summaryData      = null;
    private Map<String, Long>    bookingStatusMap = null;
    private List<SubjectReportRow> topSubjects    = null;
    private List<TutorReportRow>   topTutors      = null;

    // ── Czcionki (ustawiane w build()) ───────────────────────────────────────
    private PdfFont fontRegular;
    private PdfFont fontBold;

    // ========================================================================
    // Builder API — każda metoda zwraca this, co umożliwia łańcuchowanie
    // ========================================================================

    /**
     * Ustawia tytuł raportu widoczny w nagłówku PDF.
     *
     * @param title tytuł raportu
     * @return this
     */
    public EduLinkPdfReport title(String title) {
        this.reportTitle = title;
        return this;
    }

    /**
     * Ustawia zakres dat raportu (wyświetlany w meta-informacjach).
     *
     * @param from data początkowa (np. "2026-01-01")
     * @param to   data końcowa   (np. "2026-12-31")
     * @return this
     */
    public EduLinkPdfReport period(String from, String to) {
        this.periodFrom = from;
        this.periodTo   = to;
        return this;
    }

    /**
     * Dodaje sekcję kart podsumowania (liczby ogólne).
     *
     * @param data obiekt z danymi podsumowania
     * @return this
     */
    public EduLinkPdfReport addSummarySection(ReportSummaryData data) {
        this.summaryData = data;
        return this;
    }

    /**
     * Dodaje tabelę statusów rezerwacji.
     *
     * @param statusMap mapa: nazwa statusu → liczba rezerwacji
     * @return this
     */
    public EduLinkPdfReport addBookingStatusSection(Map<String, Long> statusMap) {
        this.bookingStatusMap = statusMap;
        return this;
    }

    /**
     * Dodaje tabelę najpopularniejszych przedmiotów.
     *
     * @param subjects lista wierszy (max 5-10 pozycji)
     * @return this
     */
    public EduLinkPdfReport addTopSubjectsSection(List<SubjectReportRow> subjects) {
        this.topSubjects = subjects;
        return this;
    }

    /**
     * Dodaje tabelę najlepszych korepetytorów.
     *
     * @param tutors lista wierszy (max 5-10 pozycji)
     * @return this
     */
    public EduLinkPdfReport addTopTutorsSection(List<TutorReportRow> tutors) {
        this.topTutors = tutors;
        return this;
    }

    // ========================================================================
    // Budowanie PDF
    // ========================================================================

    /**
     * Buduje plik PDF i zwraca jego zawartość jako tablicę bajtów.
     * Wywołaj po skonfigurowaniu wszystkich sekcji.
     *
     * @return bajty gotowego pliku PDF
     * @throws IOException jeśli wystąpi błąd inicjalizacji czcionki
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

        if (summaryData      != null) renderSummaryCards(document);
        if (bookingStatusMap != null) renderBookingStatusTable(document);
        if (topSubjects      != null) renderTopSubjectsTable(document);
        if (topTutors        != null) renderTopTutorsTable(document);

        renderFooter(document);
        document.close();

        return out.toByteArray();
    }

    // ========================================================================
    // Renderowanie sekcji
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

        meta.addCell(metaCell("Okres raportu:",
                periodFrom + " — " + periodTo, TextAlignment.LEFT));
        meta.addCell(metaCell("Wygenerowano:",
                generated, TextAlignment.RIGHT));

        doc.add(meta);
    }

    private void renderDivider(Document doc) {
        doc.add(new Paragraph().setMarginTop(4));
        // Linia jako tabela z dolnym borderem — działa we wszystkich wersjach iText 9
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
        addSectionTitle(doc, "Podsumowanie okresu");

        Table cards = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        cards.addCell(summaryCard("Rezerwacje",
                String.valueOf(summaryData.totalBookings), COLOR_PRIMARY));
        cards.addCell(summaryCard("Nowe oferty",
                String.valueOf(summaryData.newOffers), COLOR_SUCCESS));
        cards.addCell(summaryCard("Nowi użytkownicy",
                String.valueOf(summaryData.newUsers), COLOR_WARNING));

        doc.add(cards);
    }

    private void renderBookingStatusTable(Document doc) {
        addSectionTitle(doc, "Rezerwacje wg statusu");

        Table table = styledTable(new float[]{3, 1});
        table.addHeaderCell(headerCell("Status"));
        table.addHeaderCell(headerCell("Liczba"));

        boolean odd = true;
        for (Map.Entry<String, Long> entry : bookingStatusMap.entrySet()) {
            DeviceRgb bg = odd ? COLOR_SURFACE : null;
            table.addCell(dataCell(translateStatus(entry.getKey()), bg, TextAlignment.LEFT));
            table.addCell(dataCell(String.valueOf(entry.getValue()), bg, TextAlignment.CENTER));
            odd = !odd;
        }

        doc.add(table);
        doc.add(new Paragraph().setMarginBottom(20));
    }

    private void renderTopSubjectsTable(Document doc) {
        if (topSubjects.isEmpty()) return;
        addSectionTitle(doc, "Najpopularniejsze przedmioty (wg liczby rezerwacji)");

        Table table = styledTable(new float[]{0.5f, 3, 1, 1});
        table.addHeaderCell(headerCell("#"));
        table.addHeaderCell(headerCell("Przedmiot"));
        table.addHeaderCell(headerCell("Rezerwacje"));
        table.addHeaderCell(headerCell("Śr. ocena"));

        for (int i = 0; i < topSubjects.size(); i++) {
            SubjectReportRow row = topSubjects.get(i);
            DeviceRgb bg = (i % 2 == 0) ? COLOR_SURFACE : null;
            table.addCell(dataCell(String.valueOf(i + 1),        bg, TextAlignment.CENTER));
            table.addCell(dataCell(row.subjectName,              bg, TextAlignment.LEFT));
            table.addCell(dataCell(String.valueOf(row.bookingCount), bg, TextAlignment.CENTER));
            table.addCell(dataCell(
                    row.avgRating != null ? String.format("%.1f ★", row.avgRating) : "—",
                    bg, TextAlignment.CENTER));
        }

        doc.add(table);
        doc.add(new Paragraph().setMarginBottom(20));
    }

    private void renderTopTutorsTable(Document doc) {
        if (topTutors.isEmpty()) return;
        addSectionTitle(doc, "Najlepiej oceniani korepetytorzy");

        Table table = styledTable(new float[]{0.5f, 3, 1, 1});
        table.addHeaderCell(headerCell("#"));
        table.addHeaderCell(headerCell("Korepetytor"));
        table.addHeaderCell(headerCell("Rezerwacje"));
        table.addHeaderCell(headerCell("Śr. ocena"));

        for (int i = 0; i < topTutors.size(); i++) {
            TutorReportRow row = topTutors.get(i);
            DeviceRgb bg = (i % 2 == 0) ? COLOR_SURFACE : null;
            table.addCell(dataCell(String.valueOf(i + 1), bg, TextAlignment.CENTER));
            table.addCell(dataCell(row.tutorName,         bg, TextAlignment.LEFT));
            table.addCell(dataCell(String.valueOf(row.bookingCount), bg, TextAlignment.CENTER));
            table.addCell(dataCell(
                    row.avgRating != null ? String.format("%.1f", row.avgRating) : "—",
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
        doc.add(new Paragraph("EduLink © 2026 | Raport wygenerowany automatycznie przez system")
                .setFont(fontRegular).setFontSize(8)
                .setFontColor(COLOR_TEXT_SECONDARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(6));
    }

    // ========================================================================
    // Pomocnicze metody tworzenia elementów
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
            case "COMPLETED": return "Ukończone";
            case "ACCEPTED":  return "Zaakceptowane";
            case "PENDING":   return "Oczekujące";
            case "REJECTED":  return "Odrzucone";
            case "CANCELLED": return "Anulowane";
            default:          return status;
        }
    }

    // ========================================================================
    // Ładowanie czcionek z classpath
    // ========================================================================

    /**
     * Ładuje czcionkę TTF z resources biblioteki (classpath).
     * Czcionki DejaVu obsługują polskie znaki (UTF-8).
     */
    private PdfFont loadFont(String resourceName) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            // Fallback — jeśli czcionka nie jest w classpath, użyj Helvetica (bez PL znaków)
            return PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA,
                    PdfEncodings.WINANSI, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        }
        byte[] fontBytes = is.readAllBytes();
        is.close();
        return PdfFontFactory.createFont(
                fontBytes, PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
    }

    // ========================================================================
    // Klasy danych wewnętrznych (DTO biblioteki)
    // Użytkownik biblioteki tworzy te obiekty i przekazuje do buildera.
    // ========================================================================

    /**
     * Dane do sekcji kart podsumowania.
     */
    public static class ReportSummaryData {
        /** Liczba wszystkich rezerwacji w przedziale. */
        public final long totalBookings;
        /** Liczba nowych ofert w przedziale. */
        public final long newOffers;
        /** Liczba nowych użytkowników w przedziale. */
        public final long newUsers;

        public ReportSummaryData(long totalBookings, long newOffers, long newUsers) {
            this.totalBookings = totalBookings;
            this.newOffers     = newOffers;
            this.newUsers      = newUsers;
        }
    }

    /**
     * Wiersz tabeli popularnych przedmiotów.
     */
    public static class SubjectReportRow {
        /** Nazwa przedmiotu. */
        public final String subjectName;
        /** Liczba rezerwacji dla tego przedmiotu w przedziale. */
        public final long   bookingCount;
        /** Średnia ocena tutorów uczących tego przedmiotu (może być null). */
        public final Double avgRating;

        public SubjectReportRow(String subjectName, long bookingCount, Double avgRating) {
            this.subjectName  = subjectName;
            this.bookingCount = bookingCount;
            this.avgRating    = avgRating;
        }
    }

    /**
     * Wiersz tabeli top korepetytorów.
     */
    public static class TutorReportRow {
        /** Imię i nazwisko korepetytora. */
        public final String tutorName;
        /** Liczba rezerwacji w przedziale. */
        public final long   bookingCount;
        /** Średnia ocena (może być null jeśli brak opinii). */
        public final Double avgRating;

        public TutorReportRow(String tutorName, long bookingCount, Double avgRating) {
            this.tutorName    = tutorName;
            this.bookingCount = bookingCount;
            this.avgRating    = avgRating;
        }
    }
}
