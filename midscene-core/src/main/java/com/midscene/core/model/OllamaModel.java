package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.util.List;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class OllamaModel implements AIModel {

  private final ChatModel model;

  public OllamaModel(String baseUrl, String modelName) {
    this.model = OllamaChatModel.builder()
        .modelName(modelName)
        .baseUrl(baseUrl)
        .build();
  }

  @Override
  public ChatResponse chat(List<ChatMessage> messages) {
    return model.chat(messages);
  }
}
