package com.midscene.core.agent;

import com.midscene.core.agent.promt.PromptManager;
import com.midscene.core.cache.TaskCache;
import dev.langchain4j.model.chat.ChatModel;
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

  private final ChatModel chatModel;
  private final TaskCache cache;

  public Planner(ChatModel chatModel) {
    this(chatModel, TaskCache.disabled());
  }

  public Planner(ChatModel chatModel, TaskCache cache) {
    this.chatModel = chatModel;
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

    ChatResponse chatResponse = chatModel.chat(history);
    String responseJson = chatResponse.aiMessage().text();
    log.debug("AI Plan Response: {}", responseJson);
    history.add(AiMessage.from(responseJson));

    try {
      PlanningResponse planningResponse = parseXmlPlanningResponse(responseJson);
      planningResponse.setDescription(chatResponse.metadata().tokenUsage().toString());
      planningResponse.setRawResponse(responseJson);
      
      // Store in cache for first successful attempts
      if (history.size() == 2 && planningResponse.getError() == null) { 
        cache.put(instruction, planningResponse);
        log.debug("Cached planning response for instruction: {}", instruction);
      }
      
      return planningResponse;
    } catch (Exception e) {
      log.error("Failed to parse plan {}", e.getMessage());
      throw new RuntimeException("Failed to parse plan: " + responseJson, e);
    }
  }

  private PlanningResponse parseXmlPlanningResponse(String xml) {
    PlanningResponse response = new PlanningResponse();
    
    response.setThought(extractTagContent(xml, "thought"));
    response.setLog(extractTagContent(xml, "log"));
    response.setError(extractTagContent(xml, "error"));
    response.setMemory(extractTagContent(xml, "memory"));
    
    String completeContent = extractTagContent(xml, "complete");
    if (completeContent != null) {
      response.setOutput(completeContent);
      response.setFinalizeSuccess(xml.contains("success=\"true\""));
    }
    
    String actionType = extractTagContent(xml, "action-type");
    String actionParamJson = extractTagContent(xml, "action-param-json");
    
    if (actionType != null && actionParamJson != null) {
      try {
        com.midscene.core.pojo.planning.ActionsItem action = ObjectMapper.mapResponseToClass(actionParamJson,
            com.midscene.core.pojo.planning.ActionsItem.class);
        action.setType(com.midscene.core.pojo.type.AIActionType.fromValue(actionType.toLowerCase()));
        response.setActions(java.util.Collections.singletonList(action));
      } catch (Exception e) {
        log.warn("Failed to parse action params: {}", e.getMessage());
      }
    }
    
    // Parse sub-goals
    String planContent = extractTagContent(xml, "update-plan-content");
    if (planContent != null) {
      List<com.midscene.core.context.ExecutionTask.SubGoal> subGoals = new java.util.ArrayList<>();
      java.util.regex.Matcher m = java.util.regex.Pattern.compile("<sub-goal index=\"(\\d+)\" status=\"(\\w+)\">(.*?)</sub-goal>")
          .matcher(planContent);
      while (m.find()) {
        subGoals.add(com.midscene.core.context.ExecutionTask.SubGoal.builder()
            .index(Integer.parseInt(m.group(1)))
            .status(m.group(2))
            .description(m.group(3))
            .build());
      }
      response.setUpdateSubGoals(subGoals);
    }
    
    // Parse finished sub-goals
    String markFinished = extractTagContent(xml, "mark-sub-goal-done");
    if (markFinished != null) {
      List<Integer> finishedIndexes = new java.util.ArrayList<>();
      java.util.regex.Matcher m = java.util.regex.Pattern.compile("<sub-goal index=\"(\\d+)\"")
          .matcher(markFinished);
      while (m.find()) {
        finishedIndexes.add(Integer.parseInt(m.group(1)));
      }
      response.setMarkFinishedIndexes(finishedIndexes);
    }
    
    return response;
  }

  private String extractTagContent(String xml, String tagName) {
    java.util.regex.Pattern p = java.util.regex.Pattern.compile("<" + tagName + "[^>]*>(.*?)</" + tagName + ">", 
        java.util.regex.Pattern.DOTALL);
    java.util.regex.Matcher m = p.matcher(xml);
    return m.find() ? m.group(1).trim() : null;
  }

  public String query(String question, String screenshotBase64) {
    String promptText = PromptManager.constructQueryPrompt(question);
    UserMessage message = UserMessage.from(
        TextContent.from(promptText),
        ImageContent.from(screenshotBase64, "image/png"));

    log.debug("Chat Query message: {}", message);

    ChatResponse chatResponse = chatModel.chat(Collections.singletonList(message));
    String response = chatResponse.aiMessage().text();
    log.debug("AI Query Response: {}", response);
    return response;
  }

  public com.midscene.core.pojo.response.AssertionAiResponse assertCondition(String assertion, String screenshotBase64) {
    String promptText = PromptManager.constructAssertionPrompt(assertion);
    UserMessage message = UserMessage.from(
        TextContent.from(promptText),
        ImageContent.from(screenshotBase64, "image/png"));

    log.debug("Chat Assert message: {}", message);

    ChatResponse chatResponse = chatModel.chat(Collections.singletonList(message));
    String responseText = chatResponse.aiMessage().text();
    log.debug("AI Assert Response: {}", responseText);

    com.midscene.core.pojo.response.AssertionAiResponse response = new com.midscene.core.pojo.response.AssertionAiResponse();
    response.setThought(extractTagContent(responseText, "thought"));
    String pass = extractTagContent(responseText, "pass");
    response.setPass("true".equalsIgnoreCase(pass));
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
