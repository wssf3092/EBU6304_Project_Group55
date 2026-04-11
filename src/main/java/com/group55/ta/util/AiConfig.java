package com.group55.ta.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

/**
 * Loads OpenAI-compatible API settings for {@link com.group55.ta.service.AiService}.
 * <p>
 * Precedence: JVM system properties ({@code ai.*}) &gt; environment ({@code TA_AI_*}) &gt;
 * {@code ai-config.local.properties} &gt; {@code ai-config.properties}.
 * Base URL values are normalized to a concrete {@code .../v1/chat/completions} endpoint where possible.
 */
public class AiConfig {
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int timeoutMillis;
    private final int cacheMinutes;

    public AiConfig() {
        Properties properties = new Properties();
        loadProperties("ai-config.properties", properties);
        loadProperties("ai-config.local.properties", properties);

        this.baseUrl = normalizeBaseUrl(readSetting(properties, "ai.baseUrl", "TA_AI_BASE_URL", ""));
        this.apiKey = readSetting(properties, "ai.apiKey", "TA_AI_API_KEY", "");
        this.model = readSetting(properties, "ai.model", "TA_AI_MODEL", "gpt-4o-mini");
        this.timeoutMillis = readInt(properties, "ai.timeoutMillis", "TA_AI_TIMEOUT_MILLIS", 10000);
        this.cacheMinutes = readInt(properties, "ai.cacheMinutes", "TA_AI_CACHE_MINUTES", 30);
    }

    private static void loadProperties(String name, Properties target) {
        try (InputStream is = AiConfig.class.getClassLoader().getResourceAsStream(name)) {
            if (is != null) {
                target.load(is);
            }
        } catch (IOException ignored) {
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static String readSetting(Properties properties, String propertyKey, String envKey, String fallback) {
        String systemValue = System.getProperty(propertyKey);
        if (!ValidationUtil.isBlank(systemValue)) {
            return systemValue.trim();
        }

        String envValue = System.getenv(envKey);
        if (!ValidationUtil.isBlank(envValue)) {
            return envValue.trim();
        }

        String propertyValue = properties.getProperty(propertyKey);
        if (!ValidationUtil.isBlank(propertyValue)) {
            return propertyValue.trim();
        }
        return fallback;
    }

    private static int readInt(Properties properties, String propertyKey, String envKey, int fallback) {
        return parseInt(readSetting(properties, propertyKey, envKey, String.valueOf(fallback)), fallback);
    }

    private static String normalizeBaseUrl(String rawValue) {
        if (ValidationUtil.isBlank(rawValue)) {
            return "";
        }

        String value = rawValue.trim();
        try {
            URI uri = URI.create(value);
            if (ValidationUtil.isBlank(uri.getScheme()) || ValidationUtil.isBlank(uri.getHost())) {
                return value;
            }

            String path = uri.getPath() == null ? "" : uri.getPath().trim();
            path = path.replaceAll("/+$", "");
            if (ValidationUtil.isBlank(path)) {
                path = "/v1/chat/completions";
            } else if (path.endsWith("/chat/completions") || path.endsWith("/responses")) {
                // already a concrete endpoint
            } else if (path.endsWith("/models")) {
                path = "/v1/chat/completions";
            } else if (path.endsWith("/v1")) {
                path = path + "/chat/completions";
            } else {
                path = path + "/v1/chat/completions";
            }

            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, null, null).toString();
        } catch (Exception ex) {
            return value;
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public int getCacheMinutes() {
        return cacheMinutes;
    }

    /**
     * @return {@code true} when both API base URL and key are non-blank (AI calls may still fail at runtime).
     */
    public boolean isConfigured() {
        return !ValidationUtil.isBlank(baseUrl) && !ValidationUtil.isBlank(apiKey);
    }
}
