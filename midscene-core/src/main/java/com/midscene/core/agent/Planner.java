package com.midscene.core.agent;

import com.midscene.core.model.AIModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Planner {

  private static final Logger logger = LoggerFactory.getLogger(Planner.class);
  private final AIModel aiModel;

  public Planner(AIModel aiModel) {
    this.aiModel = aiModel;
  }

  public PlanningResponse plan(String instruction, String screenshotBase64, List<ChatMessage> history) {
    // Construct Message
    UserMessage message;
    if (history.isEmpty()) {
      String promptText = PromptManager.constructPlanningPrompt(instruction);
      message = UserMessage.from(
          TextContent.from(promptText),
          ImageContent.from(screenshotBase64, "image/png"));
    } else {
      // Retry with new screenshot
      message = UserMessage.from(
          TextContent.from(PromptManager.constructRetryPrompt()),
          ImageContent.from(screenshotBase64, "image/png"));
    }

    history.add(message);

    String responseJson = aiModel.chat(history);
    logger.info("AI Response: {}", responseJson);
    history.add(dev.langchain4j.data.message.AiMessage.from(responseJson));

    try {
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      return mapper.readValue(responseJson, PlanningResponse.class);
    } catch (Exception e) {
      logger.error("Failed to parse plan", e);
      throw new RuntimeException("Failed to parse plan", e);
    }
  }

  public String query(String question, String screenshotBase64) {
    String promptText = PromptManager.constructQueryPrompt(question);
    UserMessage message = UserMessage.from(
        TextContent.from(promptText),
        ImageContent.from(screenshotBase64, "image/png"));

    String response = aiModel.chat(Collections.singletonList(message));
    logger.info("AI Response: {}", response);
    return response;
  }
}
