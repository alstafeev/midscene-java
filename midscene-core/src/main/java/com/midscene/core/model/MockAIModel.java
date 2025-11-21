package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import java.util.List;

public class MockAIModel implements AIModel {

  @Override
  public String chat(List<ChatMessage> messages) {
    // Return a valid JSON plan for the Agent to execute
    // The Agent expects: { "actions": [ { "type": "TAP", "locate": { ... } } ] }
    // We'll simulate a plan to click on an element at (100, 100)
    return "{ \"actions\": [ { \"type\": \"TAP\", \"locate\": { \"left\": 100, \"top\": 100, \"width\": 50, \"height\": 50 } } ] }";
  }
}
