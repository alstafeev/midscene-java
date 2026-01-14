package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class AnthropicModel implements AIModel {

    private final ChatModel model;

    public AnthropicModel(String apiKey, String modelName, String baseUrl) {
        this.model = AnthropicChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        return model.chat(messages);
    }
}
