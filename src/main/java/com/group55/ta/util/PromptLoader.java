package com.group55.ta.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads chat-completion prompt templates from {@code resources/prompts/*.txt} and performs
 * {@code {{key}}} placeholder substitution. Templates are cached after first read.
 * <p>
 * Externalizing prompts keeps {@link com.group55.ta.service.AiService} focused on orchestration
 * and lets non-Java collaborators tune wording without touching code.
 * <p>
 * <b>Runtime override:</b> if a file {@code <data.root>/prompts/<name>.txt} exists on disk it
 * takes priority over the bundled classpath resource and is re-read on every call (no caching),
 * allowing operators to hot-swap prompt wording in production without redeployment.
 */
public final class PromptLoader {

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private PromptLoader() {
    }

    /**
     * Reads a prompt template by logical name.
     *
     * @param name template name, e.g. {@code "skills-gap.user"} → {@code prompts/skills-gap.user.txt}
     * @return raw template content (never {@code null})
     */
    public static String load(String name) {
        Path override = AppPaths.getDataRoot().resolve("prompts").resolve(name + ".txt");
        if (Files.isRegularFile(override)) {
            return readFile(override);
        }
        return CACHE.computeIfAbsent(name, PromptLoader::readResource);
    }

    /**
     * Loads {@code name} and replaces every {@code {{key}}} occurrence with the matching variable value.
     * Missing variables are replaced by an empty string.
     */
    public static String render(String name, Map<String, String> variables) {
        String template = load(name);
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() == null ? "" : entry.getValue();
            rendered = rendered.replace(placeholder, value);
        }
        return rendered.replaceAll("\\{\\{[^}]+}}", "");
    }

    /** Convenience builder so call sites stay readable. */
    public static Map<String, String> vars() {
        return new HashMap<>();
    }

    private static String readResource(String name) {
        String path = "prompts/" + name + ".txt";
        ClassLoader loader = PromptLoader.class.getClassLoader();
        try (InputStream is = loader.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Prompt template not found on classpath: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read prompt template: " + path, ex);
        }
    }

    private static String readFile(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read prompt override file: " + path, ex);
        }
    }
}
