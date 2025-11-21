package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import java.util.List;

public class SpyAIModel implements AIModel {

  public UserMessage lastMessage;

  @Override
  public String chat(List<ChatMessage> messages) {
    if (messages != null && !messages.isEmpty() && messages.get(0) instanceof UserMessage) {
      this.lastMessage = (UserMessage) messages.get(0);
    }
    // Return a dummy valid JSON to avoid parsing errors in TaskExecutor
    return "{ \"actions\": [] }";
  }

  public boolean hasImageContent() {
    if (lastMessage == null) {
      return false;
    }
    return lastMessage.contents().stream().anyMatch(c -> c instanceof ImageContent);
  }

  public boolean hasTextContent() {
    if (lastMessage == null) {
      return false;
    }
    return lastMessage.contents().stream().anyMatch(c -> c instanceof TextContent);
  }
}
