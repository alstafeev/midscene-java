package com.midscene.core.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpenAIModelTest {

  @Test
  void testConstructorAndChat() {
    // Similar to GeminiModelTest, we verify instantiation and execution path.

    OpenAIModel model = new OpenAIModel("dummy-key", "gpt-4");
    assertNotNull(model);

    List<ChatMessage> messages = Collections.singletonList(UserMessage.from("hello"));

    assertThrows(Exception.class, () -> model.chat(messages));
  }
}
