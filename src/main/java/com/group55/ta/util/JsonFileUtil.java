package com.group55.ta.util;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility for JSON file reads/writes.
 */
public final class JsonFileUtil {
    private JsonFileUtil() {
    }

    public static <T> Optional<T> read(Path path, Class<T> clazz) {
        if (path == null || !Files.exists(path)) {
            return Optional.empty();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return Optional.ofNullable(GsonProvider.gson().fromJson(reader, clazz));
        } catch (IOException | JsonSyntaxException ex) {
            return Optional.empty();
        }
    }

    /**
     * Read all {@code .json} files in a directory (sorted by filename).
     */
    public static <T> List<T> readAll(Path directory, Class<T> clazz) {
        List<T> items = new ArrayList<>();
        for (Path path : listJsonFiles(directory)) {
            read(path, clazz).ifPresent(items::add);
        }
        return items;
    }

    /** Alias matching IMPROVEMENT-PLAN naming. */
    public static <T> List<T> listAll(Path directory, Class<T> clazz) {
        return readAll(directory, clazz);
    }

    public static void write(Path path, Object payload) {
        try {
            Files.createDirectories(path.getParent());
            Path temp = path.resolveSibling(path.getFileName().toString() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GsonProvider.gson().toJson(payload, writer);
            }
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed writing JSON file: " + path, ex);
        }
    }

    public static List<Path> listJsonFiles(Path directory) {
        List<Path> files = new ArrayList<>();
        if (directory == null || !Files.exists(directory)) {
            return files;
        }
        try (Stream<Path> stream = Files.list(directory)) {
            stream
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .forEach(files::add);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed reading directory: " + directory, ex);
        }
        return files;
    }
}
