package com.group55.ta.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * AI response cache utility.
 */
public final class AiCacheUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private AiCacheUtil() {
    }

    public static Optional<JsonObject> readIfValid(Path cacheFile, Duration ttl) {
        if (!Files.exists(cacheFile)) {
            return Optional.empty();
        }
        try {
            String raw = new String(Files.readAllBytes(cacheFile), StandardCharsets.UTF_8);
            JsonObject wrapper = JsonParser.parseString(raw).getAsJsonObject();
            String cachedAt = wrapper.get("cachedAt").getAsString();
            JsonElement payload = wrapper.get("payload");
            LocalDateTime at = LocalDateTime.parse(cachedAt, FORMATTER);
            if (Duration.between(at, LocalDateTime.now()).compareTo(ttl) <= 0) {
                return Optional.of(payload.getAsJsonObject());
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    public static void write(Path cacheFile, JsonObject payload) {
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("cachedAt", LocalDateTime.now().format(FORMATTER));
        wrapper.add("payload", payload);
        try {
            Files.createDirectories(cacheFile.getParent());
            Files.write(cacheFile, GsonProvider.gson().toJson(wrapper).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }
}
