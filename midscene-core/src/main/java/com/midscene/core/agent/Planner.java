package com.midscene.core.agent;

import com.midscene.core.agent.promt.PromptManager;
import com.midscene.core.cache.TaskCache;
import com.midscene.core.model.AIModel;
import com.midscene.core.pojo.planning.PlanningResponse;
import com.midscene.core.utils.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.Collections;
import java.util.List;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Planner {

  private final AIModel aiModel;
  private final TaskCache cache;

  public Planner(AIModel aiModel) {
    this(aiModel, TaskCache.disabled());
  }

  public Planner(AIModel aiModel, TaskCache cache) {
    this.aiModel = aiModel;
    this.cache = cache != null ? cache : TaskCache.disabled();
  }

  public PlanningResponse plan(String instruction, String screenshotBase64, String pageSource,
      List<ChatMessage> history) {

    // Check cache for first attempts only (empty history means fresh attempt)
    if (history.isEmpty()) {
      PlanningResponse cached = cache.get(instruction);
      if (cached != null) {
        log.info("Cache hit for instruction: {}", instruction);
        return cached;
      }
    }

    UserMessage message;
    if (history.isEmpty()) {
      String promptText = PromptManager.constructPlanningPrompt(instruction);
      message = UserMessage.from(
          TextContent.from(promptText),
          ImageContent.from(screenshotBase64, "image/png"),
          TextContent.from(pageSource));
    } else {
      message = UserMessage.from(
          TextContent.from(PromptManager.constructRetryPrompt(instruction)),
          ImageContent.from(screenshotBase64, "image/png"),
          TextContent.from(pageSource));
    }

    history.add(message);

    log.debug("Chat Plan message: {}", message);

    ChatResponse chatResponse = aiModel.chat(history);
    String responseJson = chatResponse.aiMessage().text();
    log.debug("AI Plan Response: {}", responseJson);
    history.add(AiMessage.from(responseJson));

    try {
      PlanningResponse planningResponse = ObjectMapper.mapResponseToClass(responseJson,
          PlanningResponse.class);
      planningResponse.setDescription(chatResponse.metadata().tokenUsage().toString());
      
      // Store in cache for first successful attempts
      if (history.size() == 2) { // First attempt: 1 user message + 1 AI response
        cache.put(instruction, planningResponse);
        log.debug("Cached planning response for instruction: {}", instruction);
      }
      
      return planningResponse;
    } catch (Exception e) {
      log.error("Failed to parse plan {}", e.getMessage());
      throw new RuntimeException("Failed to parse plan", e);
    }
  }

  public String query(String question, String screenshotBase64) {
    String promptText = PromptManager.constructQueryPrompt(question);
    UserMessage message = UserMessage.from(
        TextContent.from(promptText),
        ImageContent.from(screenshotBase64, "image/png"));

    log.debug("Chat Query message: {}", message);

    ChatResponse chatResponse = aiModel.chat(Collections.singletonList(message));
    String response = chatResponse.aiMessage().text();
    log.debug("AI Query Response: {}", response);
    return response;
  }

  /**
   * Invalidates (removes) a cached plan for the given instruction.
   * Call this when execution of a cached plan fails.
   *
   * @param instruction the instruction whose cached plan should be invalidated
   * @return true if the cache entry was removed
   */
  public boolean invalidateCache(String instruction) {
    return cache.invalidate(instruction);
  }
}
