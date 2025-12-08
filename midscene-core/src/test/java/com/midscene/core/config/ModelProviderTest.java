package com.midscene.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ModelProviderTest {

  @Test
  void testEnumValues() {
    assertEquals("gpt-4o", ModelProvider.OPENAI.getModelName());
    assertEquals("gemini-3-pro-preview", ModelProvider.GEMINI.getModelName());
  }

  @Test
  void testValueOf() {
    assertEquals(ModelProvider.OPENAI, ModelProvider.valueOf("OPENAI"));
    assertEquals(ModelProvider.GEMINI, ModelProvider.valueOf("GEMINI"));
  }
}
