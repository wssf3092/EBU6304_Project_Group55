package com.group55.ta.util;

import java.io.*;
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
 * @version Sprint 0 - skeleton only
 */
public class FileStorageUtil {

    /** Base directory where all data files are stored. */
    private static final String DATA_DIR = "data/";

    /**
     * Reads the entire content of a text file and returns it as a single String.
     *
     * @param filename the name of the file (relative to {@code data/}),
     *                 e.g. {@code "users.txt"}
     * @return the full content of the file as a String, or {@code null} if an error occurs
     */
    public static String readFile(String filename) {
        // TODO: Implement file reading logic
        return null;
    }

    /**
     * Writes (overwrites) the given content to the specified file.
     * Creates the file if it does not exist.
     *
     * @param filename the name of the file (relative to {@code data/}),
     *                 e.g. {@code "users.txt"}
     * @param content  the string content to write to the file
     */
    public static void writeFile(String filename, String content) {
        // TODO: Implement file writing (overwrite) logic
    }

    /**
     * Appends a single line of text to the specified file.
     * Creates the file if it does not exist.
     *
     * @param filename the name of the file (relative to {@code data/}),
     *                 e.g. {@code "applications.csv"}
     * @param line     the line of text to append (a newline will be added automatically)
     */
    public static void appendToFile(String filename, String line) {
        // TODO: Implement file append logic
    }

    /**
     * Reads all lines from a text file and returns them as a {@link List}.
     *
     * @param filename the name of the file (relative to {@code data/}),
     *                 e.g. {@code "courses.txt"}
     * @return a {@link List} of lines, or {@code null} if an error occurs
     */
    public static List<String> readLines(String filename) {
        // TODO: Implement line-by-line file reading logic
        return null;
    }
}
