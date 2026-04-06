package com.group55.ta.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Best-effort text extraction for uploaded CV files.
 */
public final class CvTextExtractor {
    private CvTextExtractor() {
    }

    public static String extractText(Path path) {
        if (path == null || !Files.exists(path)) {
            return "";
        }
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        try {
            if (fileName.endsWith(".pdf")) {
                return normalize(extractPdf(path));
            }
            if (fileName.endsWith(".docx")) {
                return normalize(extractDocx(path));
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private static String extractPdf(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static String extractDocx(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path);
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private static String normalize(String text) {
        if (ValidationUtil.isBlank(text)) {
            return "";
        }
        String compact = text.replaceAll("\\s+", " ").trim();
        if (compact.length() > 4000) {
            return compact.substring(0, 4000);
        }
        return compact;
    }
}
