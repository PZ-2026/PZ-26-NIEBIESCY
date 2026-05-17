/*
 * Main.java
 *
 * Version: 1.0
 * Date: 2026-05-17
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package org.example;

import com.itextpdf.io.font.PdfEncodings;
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
import com.itextpdf.layout.properties.BorderCollapsePropertyValue;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.IOException;

/**
 * Invoice generator application.
 * Creates a professional PDF invoice (faktura) using iText library.
 * The invoice data is hardcoded for demonstration purposes.
 *
 * @version 1.0
 * @author EduLink Team
 */
public class Main {

    // Invoice header data
    private static final String MIEJSCOWOSC          = "WARSZAWA";
    private static final String DATA_WYSTAWIENIA      = "12.03.2015 r.";

    // Seller data
    private static final String SPRZEDAWCA_NAZWA      = "KOWALSKI JAN";
    private static final String SPRZEDAWCA_ULICA      = "UL. ZIELONA 1";
    private static final String SPRZEDAWCA_KOD        = "00-120 WARSZAWA";
    private static final String SPRZEDAWCA_NIP        = "NIP  PL 1111111111";

    // Buyer data
    private static final String NABYWCA_NAZWA         = "AVB";
    private static final String NABYWCA_ULICA         = "TOPPSTIGEN 15";
    private static final String NABYWCA_KOD           = "14951 NYNASHAMN";
    private static final String NABYWCA_KRAJ          = "SWEDEN";
    private static final String NABYWCA_NIP           = "NIP 999999999999";

    // Invoice details
    private static final String FAKTURA_NR            = "10/2015";
    private static final String DATA_DOKONANIA        = "11.03.2015 r.";
    private static final boolean PLATNOSC_PRZELEW     = true;
    private static final String TERMIN_ZAPLATY        = "28.03.2015 r.";
    private static final String NAZWA_BANKU           = "ABC";
    private static final String NR_KONTA              = "11 1111 1111 1111 1111 1111 1111";

    // Currency and exchange rate
    private static final String WALUTA                = "EUR";
    private static final String KURS_WALUTY           = "4,1233";
    private static final String TABELA_KURSOW         = "047/A/2015";
    private static final String DATA_KURSU            = "10.03.2015 r.";

    // Invoice items (product/service rows)
    private static final String[][] POZYCJE = {
            { "USŁUGA PRAWNA", "USŁ.", "1", "1 500,00" }
    };

    // Totals and notes
    private static final String RAZEM_EUR             = "1 500,00";
    private static final String RAZEM_PLN             = "6 184,95";
    private static final String DO_ZAPLATY            = "1500 EUR";
    private static final String UWAGI                 = "ODWROTNE OBCIĄŻENIE";

    /**
     * Main entry point – generates the invoice PDF file.
     *
     * @param args command line arguments (not used)
     * @throws IOException if PDF generation fails
     */
    public static void main(String[] args) throws IOException {
        String dest = "faktura.pdf";
        generujFakture(dest);
        System.out.println("Generated: " + dest);
    }

    /**
     * Creates an underlined Text object for formatting within a Paragraph.
     *
     * @param text the text to underline
     * @param font the font to use
     * @param size the font size
     * @return configured Text object with underline
     */
    private static Text underlinedText(String text, PdfFont font, float size) {
        return new Text(text)
                .setFont(font)
                .setFontSize(size)
                .setUnderline();
    }

    /**
     * Generates the invoice PDF file at the specified destination path.
     *
     * @param dest the output file path
     * @throws IOException if file writing or font loading fails
     */
    public static void generujFakture(String dest) throws IOException {
        PdfWriter writer   = new PdfWriter(dest);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc       = new Document(pdfDoc, PageSize.A4);

        doc.setMargins(36, 36, 36, 36);

        PdfFont fontNormal = PdfFontFactory.createFont(
                "C:/Windows/Fonts/arial.ttf", PdfEncodings.IDENTITY_H);
        PdfFont fontBold = PdfFontFactory.createFont(
                "C:/Windows/Fonts/arialbd.ttf", PdfEncodings.IDENTITY_H);

        float normalSize = 8f;
        float smallSize  = 7f;

        // 1. Header – location and issue date
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);

        headerTable.addCell(emptyCell());

        Table dateTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 40}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);
        dateTable.addCell(noCell(para(MIEJSCOWOSC, fontBold, normalSize, TextAlignment.CENTER)));
        dateTable.addCell(noCell(para("dnia", fontNormal, smallSize, TextAlignment.CENTER)));
        dateTable.addCell(noCell(para(DATA_WYSTAWIENIA, fontBold, normalSize, TextAlignment.CENTER)));

        Table dateLabels = new Table(UnitValue.createPercentArray(new float[]{40, 20, 40}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);
        dateLabels.addCell(noCell(para("miejscowość", fontNormal, 6f, TextAlignment.CENTER)));
        dateLabels.addCell(noCell(para("", fontNormal, 6f, TextAlignment.CENTER)));
        dateLabels.addCell(noCell(para("data wystawienia", fontNormal, 6f, TextAlignment.CENTER)));

        Cell rightHeader = new Cell().setBorder(Border.NO_BORDER);
        rightHeader.add(dateTable);
        rightHeader.add(dateLabels);
        headerTable.addCell(rightHeader);
        doc.add(headerTable);

        // 2. Seller / Buyer boxes
        Table partyTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth()
                .setMarginTop(6f);

        // Seller
        Cell sprzedawcaCell = new Cell()
                .setBorder(new SolidBorder(1f))
                .setPadding(5f);
        sprzedawcaCell.add(para("Sprzedawca", fontNormal, smallSize, TextAlignment.LEFT));
        sprzedawcaCell.add(para(SPRZEDAWCA_NAZWA, fontBold, normalSize, TextAlignment.LEFT));
        sprzedawcaCell.add(para(SPRZEDAWCA_ULICA, fontBold, normalSize, TextAlignment.LEFT));
        sprzedawcaCell.add(para(SPRZEDAWCA_KOD,   fontBold, normalSize, TextAlignment.LEFT));
        sprzedawcaCell.add(new Paragraph("\n").setFontSize(4f));
        sprzedawcaCell.add(para(SPRZEDAWCA_NIP, fontNormal, normalSize, TextAlignment.LEFT));
        partyTable.addCell(sprzedawcaCell);

        // Buyer
        Cell nabywcaCell = new Cell()
                .setBorder(new SolidBorder(1f))
                .setPadding(5f)
                .setMarginRight(8f);
        nabywcaCell.add(para("Nabywca", fontNormal, smallSize, TextAlignment.LEFT));
        nabywcaCell.add(para(NABYWCA_NAZWA,  fontBold, normalSize, TextAlignment.LEFT));
        nabywcaCell.add(para(NABYWCA_ULICA,  fontBold, normalSize, TextAlignment.LEFT));
        nabywcaCell.add(para(NABYWCA_KOD,    fontBold, normalSize, TextAlignment.LEFT));
        nabywcaCell.add(para(NABYWCA_KRAJ,   fontBold, normalSize, TextAlignment.LEFT));
        nabywcaCell.add(new Paragraph("\n").setFontSize(4f));
        nabywcaCell.add(para(NABYWCA_NIP, fontNormal, normalSize, TextAlignment.LEFT));
        partyTable.addCell(nabywcaCell);

        doc.add(partyTable);

        // 3. Title – "FAKTURA Nr"
        Table titleTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth()
                .setMarginTop(12f);

        Cell titleCell = new Cell(1, 1)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        titleCell.add(para("FAKTURA Nr", fontBold, 16f, TextAlignment.RIGHT));
        titleTable.addCell(titleCell);

        Cell nrCell = new Cell()
                .setBorder(new SolidBorder(1f))
                .setPadding(4f)
                .setTextAlignment(TextAlignment.CENTER);
        nrCell.add(para(FAKTURA_NR, fontBold, 14f, TextAlignment.CENTER));
        titleTable.addCell(nrCell);

        doc.add(titleTable);

        // 4. Delivery date
        Paragraph dataDokonania = new Paragraph()
                .setFontSize(smallSize)
                .setMarginTop(8f);
        dataDokonania.add(new Text("Data dokonania lub zakończenia dostawy towarów lub wykonania usługi")
                .setFont(fontNormal).setFontSize(smallSize));
        dataDokonania.add(new Text("1)").setFont(fontNormal).setFontSize(5f));
        dataDokonania.add(new Text(": ").setFont(fontNormal).setFontSize(smallSize));
        dataDokonania.add(underlinedText(DATA_DOKONANIA, fontBold, smallSize));
        doc.add(dataDokonania);

        // 5. Payment method and bank details
        Table payTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth()
                .setMarginTop(6f)
                .setBorder(Border.NO_BORDER);

        Cell payLeft = new Cell().setBorder(Border.NO_BORDER).setPadding(2f);
        payLeft.add(para("Sposób zapłaty:", fontNormal, smallSize, TextAlignment.LEFT));
        payLeft.add(para((PLATNOSC_PRZELEW ? "☐" : "☑") + " gotówka", fontNormal, smallSize, TextAlignment.LEFT));
        payLeft.add(para((PLATNOSC_PRZELEW ? "☑" : "☐") + " przelew",  fontNormal, smallSize, TextAlignment.LEFT));
        payTable.addCell(payLeft);

        Cell payRight = new Cell().setBorder(Border.NO_BORDER).setPadding(2f);
        payRight.add(labelValue("Termin zapłaty: ", TERMIN_ZAPLATY,  fontNormal, fontBold, smallSize));
        payRight.add(labelValue("Nazwa Banku: ",   NAZWA_BANKU,     fontNormal, fontBold, smallSize));
        payRight.add(labelValue("Nr konta: ",      NR_KONTA,        fontNormal, fontBold, smallSize));
        payTable.addCell(payRight);

        doc.add(payTable);

        // 6. Currency and exchange rate
        Table kursTable = new Table(UnitValue.createPercentArray(new float[]{15, 20, 30, 20, 15}))
                .useAllAvailableWidth()
                .setMarginTop(6f)
                .setBorder(Border.NO_BORDER);

        kursTable.addCell(noCell(new Paragraph()
                .setFontSize(smallSize).setMultipliedLeading(1.3f)
                .add(new Text("Waluta: ").setFont(fontNormal))
                .add(underlinedText(WALUTA, fontBold, smallSize))));

        kursTable.addCell(noCell(new Paragraph()
                .setFontSize(smallSize).setMultipliedLeading(1.3f)
                .add(new Text("Kurs waluty: ").setFont(fontNormal))
                .add(underlinedText(KURS_WALUTY, fontBold, smallSize))));

        kursTable.addCell(noCell(new Paragraph()
                .setFontSize(smallSize).setMultipliedLeading(1.3f)
                .add(new Text("tabela kursów średnich NBP nr ").setFont(fontNormal))
                .add(underlinedText(TABELA_KURSOW, fontBold, smallSize))));

        kursTable.addCell(noCell(new Paragraph()
                .setFontSize(smallSize).setMultipliedLeading(1.3f)
                .add(new Text("z dnia ").setFont(fontNormal))
                .add(underlinedText(DATA_KURSU, fontBold, smallSize))));

        kursTable.addCell(noCell(para("", fontNormal, smallSize, TextAlignment.LEFT)));
        doc.add(kursTable);

        // 7. Invoice items table
        float[] cols = {5, 40, 8, 8, 13, 13, 13};
        Table itemTable = new Table(UnitValue.createPercentArray(cols))
                .useAllAvailableWidth()
                .setMarginTop(8f)
                .setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);

        // Headers
        String[] headers = {"Lp.", "Nazwa towaru / usługi", "J.m.", "Ilość",
                "Cena jedn.\nnetto\nEUR", "Wartość\nEUR", "Wartość\nPLN"};
        for (String h : headers) {
            Cell hCell = new Cell()
                    .setBorder(new SolidBorder(0.5f))
                    .setBackgroundColor(new DeviceRgb(255, 0, 0))
                    .setPadding(3f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            hCell.add(new Paragraph(h).setFont(fontBold).setFontSize(smallSize)
                    .setMultipliedLeading(1.1f));
            itemTable.addHeaderCell(hCell);
        }

        // Item rows
        for (int i = 0; i < POZYCJE.length; i++) {
            String[] p = POZYCJE[i];
            float cenaNetto   = parseAmount(p[3]);
            float ilosc       = Float.parseFloat(p[2].replace(",", "."));
            float kurs        = Float.parseFloat(KURS_WALUTY.replace(",", "."));
            float wartoscEUR  = cenaNetto * ilosc;
            float wartoscPLN  = wartoscEUR * kurs;

            addItemRow(itemTable, fontNormal, fontBold, smallSize,
                    String.valueOf(i + 1) + ".",
                    p[0], p[1], p[2], p[3],
                    formatAmount(wartoscEUR),
                    formatAmount(wartoscPLN));
        }

        // Empty rows for visual balance
        for (int i = 0; i < 4; i++) {
            for (int c = 0; c < 7; c++) {
                itemTable.addCell(new Cell()
                        .setBorder(new SolidBorder(0.5f))
                        .setMinHeight(18f)
                        .add(new Paragraph(" ").setFontSize(smallSize)));
            }
        }

        // Totals row
        Cell razemLabel = new Cell(1, 5)
                .setBorder(new SolidBorder(0.5f))
                .setPadding(3f)
                .setTextAlignment(TextAlignment.RIGHT);
        razemLabel.add(para("Razem", fontBold, normalSize, TextAlignment.RIGHT));
        itemTable.addCell(razemLabel);

        itemTable.addCell(new Cell()
                .setBorder(new SolidBorder(0.5f))
                .setPadding(3f)
                .add(para(RAZEM_EUR, fontBold, normalSize, TextAlignment.RIGHT)));
        itemTable.addCell(new Cell()
                .setBorder(new SolidBorder(0.5f))
                .setPadding(3f)
                .add(para(RAZEM_PLN, fontBold, normalSize, TextAlignment.RIGHT)));

        doc.add(itemTable);

        // 8. Total amount due
        doc.add(new Paragraph()
                .setMarginTop(10f)
                .setFontSize(normalSize)
                .add(new Text("Do zapłaty:  ").setFont(fontNormal))
                .add(new Text(DO_ZAPLATY).setFont(fontBold)));

        // 9. Remarks
        doc.add(new Paragraph("\n").setFontSize(4f));
        Table uwagiTable = new Table(UnitValue.createPercentArray(new float[]{100}))
                .useAllAvailableWidth();
        Cell uwagiCell = new Cell()
                .setBorder(new SolidBorder(1f))
                .setPadding(5f);
        uwagiCell.add(para("Uwagi:", fontNormal, smallSize, TextAlignment.LEFT));
        uwagiCell.add(para(UWAGI, fontBold, 12f, TextAlignment.LEFT));
        uwagiTable.addCell(uwagiCell);
        doc.add(uwagiTable);

        // 10. Footnote
        doc.add(new Paragraph()
                .setMarginTop(20f)
                .setFontSize(6f)
                .add(new Text("1) ").setFont(fontBold))
                .add(new Text("o ile taka data jest określona i różni się od daty wystawienia faktury")
                        .setFont(fontNormal)));

        doc.close();
    }

    /**
     * Creates a simple paragraph with specified font, size and alignment.
     *
     * @param text  the paragraph content
     * @param font  the font to use
     * @param size  the font size
     * @param align text alignment
     * @return configured Paragraph
     */
    private static Paragraph para(String text, PdfFont font, float size, TextAlignment align) {
        return new Paragraph(text)
                .setFont(font)
                .setFontSize(size)
                .setTextAlignment(align)
                .setMultipliedLeading(1.2f);
    }

    /**
     * Creates a paragraph with a label (normal font) and a value (bold font) on the same line.
     *
     * @param label      the label text
     * @param value      the value text
     * @param fontNormal normal font for label
     * @param fontBold   bold font for value
     * @param size       font size for both
     * @return configured Paragraph
     */
    private static Paragraph labelValue(String label, String value,
                                        PdfFont fontNormal, PdfFont fontBold, float size) {
        return new Paragraph()
                .setFontSize(size)
                .setMultipliedLeading(1.3f)
                .add(new Text(label).setFont(fontNormal))
                .add(new Text(value).setFont(fontBold));
    }

    /**
     * Creates a table cell without borders and with minimal padding.
     *
     * @param content the content to place inside the cell
     * @return configured Cell
     */
    private static Cell noCell(IBlockElement content) {
        return new Cell().setBorder(Border.NO_BORDER).setPadding(1f).add(content);
    }

    /**
     * Creates an empty cell with a single space (useful for layout tables).
     *
     * @return empty Cell without borders
     */
    private static Cell emptyCell() {
        return new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph(" ").setFontSize(1f));
    }

    /**
     * Adds a complete item row to the invoice items table.
     *
     * @param table       the table to add to
     * @param fontNormal  normal font
     * @param fontBold    bold font
     * @param size        font size
     * @param lp          line number
     * @param nazwa       product/service name
     * @param jm          unit of measure
     * @param ilosc       quantity
     * @param cenaNetto   net price per unit
     * @param wartoscEUR  total value in EUR
     * @param wartoscPLN  total value in PLN
     */
    private static void addItemRow(Table table, PdfFont fontNormal, PdfFont fontBold,
                                   float size, String lp, String nazwa, String jm,
                                   String ilosc, String cenaNetto,
                                   String wartoscEUR, String wartoscPLN) {
        addItemCell(table, lp,        fontNormal, size, TextAlignment.CENTER);
        addItemCell(table, nazwa,     fontBold,   size, TextAlignment.LEFT);
        addItemCell(table, jm,        fontNormal, size, TextAlignment.CENTER);
        addItemCell(table, ilosc,     fontNormal, size, TextAlignment.CENTER);
        addItemCell(table, cenaNetto, fontNormal, size, TextAlignment.RIGHT);
        addItemCell(table, wartoscEUR,fontNormal, size, TextAlignment.RIGHT);
        addItemCell(table, wartoscPLN,fontNormal, size, TextAlignment.RIGHT);
    }

    /**
     * Helper method to add a single cell to the items table.
     *
     * @param table the target table
     * @param text  cell content
     * @param font  font to use
     * @param size  font size
     * @param align text alignment
     */
    private static void addItemCell(Table table, String text, PdfFont font,
                                    float size, TextAlignment align) {
        table.addCell(new Cell()
                .setBorder(new SolidBorder(0.5f))
                .setPadding(3f)
                .add(new Paragraph(text).setFont(font).setFontSize(size)
                        .setTextAlignment(align)));
    }

    /**
     * Parses a Polish currency string (e.g., "1 500,00") into a float.
     *
     * @param s the amount string
     * @return float value
     */
    private static float parseAmount(String s) {
        return Float.parseFloat(s.replace(" ", "").replace(",", "."));
    }

    /**
     * Formats a float value into Polish currency format with spaces as thousand separators.
     * Example: 1500.00 → "1 500,00"
     *
     * @param val the float amount
     * @return formatted string
     */
    private static String formatAmount(float val) {
        long cents = Math.round(val * 100);
        long whole = cents / 100;
        long frac  = cents % 100;
        StringBuilder sb = new StringBuilder(String.valueOf(whole));
        int len = sb.length();
        for (int i = len - 3; i > 0; i -= 3) {
            sb.insert(i, ' ');
        }
        return sb + "," + String.format("%02d", frac);
    }
}