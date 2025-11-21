package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.util.List;

public class OpenAIModel implements AIModel {

  private final ChatLanguageModel model;

  public OpenAIModel(String apiKey, String modelName) {
    this.model = OpenAiChatModel.builder()
        .apiKey(apiKey)
        .modelName(modelName)
        .build();
  }

  @Override
  public String chat(List<ChatMessage> messages) {
    return model.generate(messages).content().text();
  }
}
