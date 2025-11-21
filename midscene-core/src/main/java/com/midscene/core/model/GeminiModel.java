package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import java.util.List;

public class GeminiModel implements AIModel {

  private final ChatLanguageModel model;

  public GeminiModel(String apiKey, String modelName) {
    this.model = GoogleAiGeminiChatModel.builder()
        .apiKey(apiKey)
        .modelName(modelName)
        .build();
  }

  @Override
  public String chat(List<ChatMessage> messages) {
    return model.generate(messages).content().text();
  }
}
