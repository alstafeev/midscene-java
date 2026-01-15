package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.List;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AzureOpenAiModel implements AIModel {

  private final ChatModel model;

  public AzureOpenAiModel(String apiKey, String baseUrl) {
    this.model = AzureOpenAiChatModel.builder()
        .endpoint(baseUrl)
        .apiKey(apiKey)
        .build();
  }

  @Override
  public ChatResponse chat(List<ChatMessage> messages) {
    return model.chat(messages);
  }
}
