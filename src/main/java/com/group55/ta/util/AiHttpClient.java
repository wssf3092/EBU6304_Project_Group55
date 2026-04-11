package com.group55.ta.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HttpURLConnection based AI REST caller.
 */
public class AiHttpClient {
    private final AiConfig config;

    public AiHttpClient(AiConfig config) {
        this.config = config;
    }

    public JsonObject requestJson(String systemPrompt, String userPrompt) {
        JsonObject fallback = new JsonObject();
        fallback.addProperty("available", false);
        fallback.addProperty("message", "AI analysis is temporarily unavailable.");

        if (!config.isConfigured()) {
            fallback.addProperty("message", "AI configuration missing.");
            return fallback;
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(config.getBaseUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            conn.setConnectTimeout(config.getTimeoutMillis());
            conn.setReadTimeout(config.getTimeoutMillis());
            conn.setDoOutput(true);

            JsonObject body = new JsonObject();
            body.addProperty("model", config.getModel());
            JsonArray messages = new JsonArray();
            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content", systemPrompt);
            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", userPrompt);
            messages.add(system);
            messages.add(user);
            body.add("messages", messages);
            body.addProperty("temperature", 0.2);
            body.addProperty("stream", false);
            JsonObject responseFormat = new JsonObject();
            responseFormat.addProperty("type", "json_object");
            body.add("response_format", responseFormat);

            try (OutputStream os = conn.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                writer.write(GsonProvider.gson().toJson(body));
            }

            int code = conn.getResponseCode();
            InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
            String text;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                text = builder.toString();
            }

            if (code < 200 || code >= 300) {
                fallback.addProperty("message", extractErrorMessage(text, code));
                return fallback;
            }

            JsonObject response = JsonParser.parseString(text).getAsJsonObject();
            String content = extractMessageContent(response);

            JsonObject parsed;
            try {
                parsed = JsonParser.parseString(content).getAsJsonObject();
            } catch (Exception ex) {
                parsed = new JsonObject();
                parsed.addProperty("summary", content);
            }
            parsed.addProperty("available", true);
            return parsed;
        } catch (Exception ex) {
            fallback.addProperty("message", "AI call failed. " + ex.getMessage());
            return fallback;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String extractErrorMessage(String responseBody, int statusCode) {
        try {
            JsonObject payload = JsonParser.parseString(responseBody).getAsJsonObject();
            if (payload.has("error") && payload.get("error").isJsonObject()) {
                JsonObject error = payload.getAsJsonObject("error");
                if (error.has("message")) {
                    return error.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {
        }
        return "AI API error: HTTP " + statusCode;
    }

    private static String extractMessageContent(JsonObject response) {
        JsonElement choicesElement = response.get("choices");
        if (choicesElement == null || !choicesElement.isJsonArray() || choicesElement.getAsJsonArray().size() == 0) {
            return "{}";
        }

        JsonObject message = choicesElement.getAsJsonArray()
                .get(0).getAsJsonObject()
                .getAsJsonObject("message");
        if (message == null || !message.has("content")) {
            return "{}";
        }

        JsonElement content = message.get("content");
        if (content.isJsonPrimitive()) {
            return content.getAsString();
        }
        if (content.isJsonArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonElement part : content.getAsJsonArray()) {
                if (!part.isJsonObject()) {
                    continue;
                }
                JsonObject item = part.getAsJsonObject();
                if (item.has("text")) {
                    builder.append(item.get("text").getAsString());
                }
            }
            return builder.length() == 0 ? "{}" : builder.toString();
        }
        return "{}";
    }
}
