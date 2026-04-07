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
 * <p><b>MVC Role:</b> Part of the DAO / utility layer</p>
 *
 * @author Group 55
 */
public class FileStorageUtil {

    /**
     * Reads the entire content of a text file and returns it as a single String.
     *
     * @param filename the file path (relative to working directory),
     *                 e.g. {@code "data/users.txt"}
     * @return the full content of the file as a String, or {@code null} if an error occurs
     */
    public static String readFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (!first) {
                    sb.append("\n");
                }
                sb.append(line);
                first = false;
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Writes (overwrites) the given content to the specified file.
     * Creates the file and parent directories if they do not exist.
     *
     * @param filename the file path (relative to working directory),
     *                 e.g. {@code "data/users.txt"}
     * @param content  the string content to write to the file
     * @return {@code true} if the write succeeded, {@code false} otherwise
     */
    public static boolean writeFile(String filename, String content) {
        File file = new File(filename);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            writer.write(content == null ? "" : content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Appends content to the specified file.
     * Creates the file and parent directories if they do not exist.
     *
     * @param filename the file path (relative to working directory),
     *                 e.g. {@code "data/applications.csv"}
     * @param line     the text to append (caller controls newline placement)
     * @return {@code true} if the append succeeded, {@code false} otherwise
     */
    public static boolean appendToFile(String filename, String line) {
        File file = new File(filename);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"))) {
            writer.write(line == null ? "" : line);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads all lines from a text file and returns them as a {@link List}.
     * Empty lines and lines starting with {@code #} (comments) are skipped.
     *
     * @param filename the file path (relative to working directory),
     *                 e.g. {@code "data/courses.txt"}
     * @return a {@link List} of lines, or {@code null} if an error occurs
     */
    public static List<String> readLines(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                    lines.add(line);
                }
            }
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
