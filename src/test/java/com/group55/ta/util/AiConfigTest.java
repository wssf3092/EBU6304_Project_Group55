package com.group55.ta.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regression tests for {@link AiConfig} URL normalization (no network). */
class AiConfigTest {
    @AfterEach
    void tearDown() {
        System.clearProperty("ai.baseUrl");
        System.clearProperty("ai.apiKey");
        System.clearProperty("ai.model");
        System.clearProperty("ai.timeoutMillis");
        System.clearProperty("ai.cacheMinutes");
    }

    @Test
    void modelsPageBaseUrlIsNormalizedToChatCompletionsEndpoint() {
        System.setProperty("ai.baseUrl", "https://api.bltcy.ai/models");
        System.setProperty("ai.apiKey", "test-key");
        System.setProperty("ai.model", "gpt5.4");

        AiConfig config = new AiConfig();

        assertEquals("https://api.bltcy.ai/v1/chat/completions", config.getBaseUrl());
        assertEquals("gpt5.4", config.getModel());
        assertTrue(config.isConfigured());
    }

    @Test
    void v1BaseUrlIsNormalizedToConcreteChatCompletionsEndpoint() {
        System.setProperty("ai.baseUrl", "https://api.bltcy.ai/v1");
        System.setProperty("ai.apiKey", "test-key");

        AiConfig config = new AiConfig();

        assertEquals("https://api.bltcy.ai/v1/chat/completions", config.getBaseUrl());
    }

    @Test
    void deepSeekBaseUrlWithoutPathIsNormalizedToConcreteChatCompletionsEndpoint() {
        System.setProperty("ai.baseUrl", "https://api.deepseek.com");
        System.setProperty("ai.apiKey", "test-key");
        System.setProperty("ai.model", "deepseek-chat");

        AiConfig config = new AiConfig();

        assertEquals("https://api.deepseek.com/v1/chat/completions", config.getBaseUrl());
        assertEquals("deepseek-chat", config.getModel());
    }
}
