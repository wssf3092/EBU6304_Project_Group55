package com.group55.ta.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for all file-based data persistence in this application.
 *
 * <p><b>IMPORTANT CONSTRAINT:</b> All data persistence in this project
 * <em>must</em> go through this utility class. The use of any database
 * (relational or NoSQL) is strictly prohibited. All data is stored in
 * plain text files ({@code .txt}, {@code .csv}, or {@code .json}) located
 * in the {@code data/} directory at the project root.</p>
 *
 * <p>This design keeps the application lightweight and dependency-free,
 * consistent with the project's no-framework, no-database requirement.</p>
 *
 * <p><b>MVC Role:</b> Part of the DAO / utility layer</p>
 *
 * @author Group 55
 * @version Sprint 1
 */
public class FileStorageUtil {

    /** Base directory where all data files are stored. */
    private static final String DATA_DIR = "data/";

    /**
     * Reads the entire content of a text file and returns it as a single String.
     *
     * <p>Lines are joined with the system line separator. Blank lines and comment
     * lines (starting with {@code #}) are preserved as-is.</p>
     *
     * @param filename the name of the file (relative to {@code data/}),
     *                 e.g. {@code "users.txt"}
     * @return the full content of the file as a String, or {@code null} if the
     *         file does not exist or an I/O error occurs
     */
    public static String readFile(String filename) {
        File file = new File(DATA_DIR + filename);
        if (!file.exists()) {
            System.out.println("[FileStorageUtil] File not found: " + file.getPath());
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("[FileStorageUtil] Error reading file: " + file.getPath()
                    + " — " + e.getMessage());
            return null;
        }
        return sb.toString();
    }

    /**
     * Writes (overwrites) the given content to the specified file.
     * Creates the parent directory and the file if they do not exist.
     *
     * @param filename the name of the file (relative to {@code data/}),
     *                 e.g. {@code "users.txt"}
     * @param content  the string content to write to the file
     */
    public static void writeFile(String filename, String content) {
        File file = new File(DATA_DIR + filename);
        // Ensure parent directory exists
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"))) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("[FileStorageUtil] Error writing file: " + file.getPath()
                    + " — " + e.getMessage());
        }
    }

    /**
     * Appends a single line of text to the specified file.
     * Creates the parent directory and the file if they do not exist.
     * A newline character is automatically appended after the line.
     *
     * @param filename the name of the file (relative to {@code data/}),
     *                 e.g. {@code "applications.txt"}
     * @param line     the line of text to append (newline is added automatically)
     */
    public static void appendToFile(String filename, String line) {
        File file = new File(DATA_DIR + filename);
        // Ensure parent directory exists
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("[FileStorageUtil] Error appending to file: " + file.getPath()
                    + " — " + e.getMessage());
        }
    }

    /**
     * Reads all lines from a text file and returns them as a {@link List}.
     * Blank lines and comment lines (starting with {@code #}) are included
     * in the returned list; callers should filter them as needed.
     *
     * @param filename the name of the file (relative to {@code data/}),
     *                 e.g. {@code "courses.txt"}
     * @return a {@link List} of raw lines, or {@code null} if the file does
     *         not exist or an I/O error occurs
     */
    public static List<String> readLines(String filename) {
        File file = new File(DATA_DIR + filename);
        if (!file.exists()) {
            System.out.println("[FileStorageUtil] File not found: " + file.getPath());
            return null;
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("[FileStorageUtil] Error reading lines from file: " + file.getPath()
                    + " — " + e.getMessage());
            return null;
        }
        return lines;
    }
}
