package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.List;
import lombok.extern.log4j.Log4j2;

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
