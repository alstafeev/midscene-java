package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

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
