package com.midscene.core.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MidsceneConfigTest {

    @Test
    void testBuilderDefaults() {
        MidsceneConfig config = MidsceneConfig.builder()
                .apiKey("test-key")
                .build();

        assertEquals(ModelProvider.OPENAI, config.getProvider());
        assertEquals("test-key", config.getApiKey());
        assertEquals("gpt-4o", config.getModelName());
        assertEquals(30000, config.getTimeoutMs());
    }

    @Test
    void testBuilderWithCustomValues() {
        MidsceneConfig config = MidsceneConfig.builder()
                .provider(ModelProvider.GEMINI)
                .apiKey("gemini-key")
                .modelName("gemini-1.5-pro")
                .timeoutMs(60000)
                .build();

        assertEquals(ModelProvider.GEMINI, config.getProvider());
        assertEquals("gemini-key", config.getApiKey());
        assertEquals("gemini-1.5-pro", config.getModelName());
        assertEquals(60000, config.getTimeoutMs());
    }

    @Test
    void testMissingApiKeyThrowsException() {
        MidsceneConfig.Builder builder = MidsceneConfig.builder();
        Exception exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertEquals("API Key must be provided", exception.getMessage());
    }

    @Test
    void testEmptyApiKeyThrowsException() {
        MidsceneConfig.Builder builder = MidsceneConfig.builder().apiKey("");
        Exception exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertEquals("API Key must be provided", exception.getMessage());
    }

    @Test
    void testDefaultModelNameForGemini() {
        MidsceneConfig config = MidsceneConfig.builder()
                .provider(ModelProvider.GEMINI)
                .apiKey("key")
                .build();

        assertEquals("gemini-3-pro-preview", config.getModelName());
    }
}
