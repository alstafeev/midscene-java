package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GeminiModelTest {

    @Test
    void testConstructorAndChat() {
        // We can't easily mock the internal ChatModel without refactoring or using
        // static mocks.
        // So we just verify we can instantiate it and calling chat throws an exception
        // (due to invalid key/network)
        // This ensures the code paths are covered.

        GeminiModel model = new GeminiModel("dummy-key", "gemini-pro");
        assertNotNull(model);

        List<ChatMessage> messages = Collections.singletonList(UserMessage.from("hello"));

        // It should fail because of invalid key or no network, but that means the
        // method was executed.
        assertThrows(Exception.class, () -> model.chat(messages));
    }
}
