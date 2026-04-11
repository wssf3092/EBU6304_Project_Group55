package com.group55.ta.util;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * CV storage helpers.
 */
public final class CvFileUtil {
    private static final long MAX_BYTES = 5L * 1024L * 1024L;

    private CvFileUtil() {
    }

    public static String saveUpload(String userId, Part part) {
        if (ValidationUtil.isBlank(userId)) {
            throw new IllegalArgumentException("User id is required to store a CV.");
        }
        if (part == null || part.getSize() <= 0) {
            throw new IllegalArgumentException("Select a PDF or DOCX file to upload.");
        }
        if (part.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("CV file size must stay within 5 MB.");
        }
        String submitted = ValidationUtil.sanitizeFileName(part.getSubmittedFileName());
        String lower = submitted.toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".pdf") || lower.endsWith(".docx"))) {
            throw new IllegalArgumentException("Only PDF and DOCX files are allowed.");
        }

        Path directory = AppPaths.cvs().resolve(userId);
        try {
            Files.createDirectories(directory);
            try (Stream<Path> stream = Files.list(directory)) {
                stream.filter(Files::isRegularFile).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ex) {
                        throw new IllegalStateException("Unable to replace the existing CV file.", ex);
                    }
                });
            }
            Path target = directory.resolve(submitted);
            try (InputStream inputStream = part.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return submitted;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save the uploaded CV file.", ex);
        }
    }

    public static Optional<Path> findCv(String userId) {
        if (ValidationUtil.isBlank(userId)) {
            return Optional.empty();
        }
        Path directory = AppPaths.cvs().resolve(userId);
        if (!Files.exists(directory)) {
            return Optional.empty();
        }
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(Files::isRegularFile).findFirst();
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    public static String contentType(Path path) {
        if (path == null) {
            return "application/octet-stream";
        }
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (fileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "application/octet-stream";
    }
}
