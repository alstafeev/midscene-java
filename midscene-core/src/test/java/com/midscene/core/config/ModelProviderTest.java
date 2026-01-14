package com.midscene.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ModelProviderTest {

  @Test
  void testEnumValues() {
    assertEquals("gpt-4o", ModelProvider.OPENAI.getModelName());
    assertEquals("gemini-3-pro-preview", ModelProvider.GEMINI.getModelName());
    assertEquals("claude-3-5-sonnet-20240620", ModelProvider.ANTHROPIC.getModelName());
    assertEquals("small-latest", ModelProvider.MISTRAL.getModelName());
    assertEquals("llama3.1", ModelProvider.OLLAMA.getModelName());
  }

  @Test
  void testValueOf() {
    assertEquals(ModelProvider.OPENAI, ModelProvider.valueOf("OPENAI"));
    assertEquals(ModelProvider.GEMINI, ModelProvider.valueOf("GEMINI"));
  }
}
