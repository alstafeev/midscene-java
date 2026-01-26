package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.util.List;

public class QwenModel implements AIModel {

  private final ChatModel model;

  public QwenModel(String apiKey, String modelName, String baseUrl) {
    this.model = OpenAiChatModel.builder()
        .apiKey(apiKey)
        .modelName(modelName)
        .baseUrl(baseUrl)
        .build();
  }

  @Override
  public ChatResponse chat(List<ChatMessage> messages) {
    return model.chat(messages);
  }
}
