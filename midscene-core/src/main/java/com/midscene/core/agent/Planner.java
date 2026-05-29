package com.midscene.core.agent;

import com.midscene.core.agent.promt.PromptManager;
import com.midscene.core.cache.TaskCache;
import dev.langchain4j.model.chat.ChatModel;
import com.midscene.core.config.PlanningStrategy;
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
  private TaskCache cache;
  private PlanningStrategy planningStrategy = PlanningStrategy.STANDARD;

  public Planner(ChatModel chatModel) {
    this(chatModel, TaskCache.disabled());
  }

  public Planner(ChatModel chatModel, TaskCache cache) {
    this.chatModel = chatModel;
    this.cache = cache != null ? cache : TaskCache.disabled();
  }

  public void setCache(TaskCache cache) {
    this.cache = cache != null ? cache : TaskCache.disabled();
  }

  public PlanningStrategy getPlanningStrategy() {
    return planningStrategy;
  }

  public void setPlanningStrategy(PlanningStrategy strategy) {
    this.planningStrategy = strategy != null ? strategy : PlanningStrategy.STANDARD;
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
    if (planningStrategy == PlanningStrategy.UI_TARS) {
      if (history.isEmpty()) {
        String promptText = "You are a GUI agent. You are given a task and your action history, with screenshots. You need to perform the next action to complete the task. \n" +
            "\n" +
            "## Output Format\n" +
            "```\n" +
            "Thought: ...\n" +
            "Action: ...\n" +
            "```\n" +
            "\n" +
            "## Action Space\n" +
            "\n" +
            "click(start_box='[x1, y1, x2, y2]')\n" +
            "left_double(start_box='[x1, y1, x2, y2]')\n" +
            "right_single(start_box='[x1, y1, x2, y2]')\n" +
            "drag(start_box='[x1, y1, x2, y2]', end_box='[x3, y3, x4, y4]')\n" +
            "hotkey(key='')\n" +
            "type(content='xxx') # Use escape characters \\', \\\", and \\n in content part to ensure we can parse the content in normal python string format. If you want to submit your input, use \\n at the end of content. \n" +
            "scroll(start_box='[x1, y1, x2, y2]', direction='down or up or right or left')\n" +
            "wait() #Sleep for 5s and take a screenshot to check for any changes.\n" +
            "finished(content='xxx') # Use escape characters \\', \\\", and \\n in content part to ensure we can parse the content in normal python string format.\n" +
            "\n" +
            "\n" +
            "## Note\n" +
            "- Use English in `Thought` part.\n" +
            "- Write a small plan and finally summarize your next action (with its target element) in one sentence in `Thought` part.\n" +
            "\n" +
            "## User Instruction\n" + instruction;
        message = UserMessage.from(
            TextContent.from(promptText),
            ImageContent.from(screenshotBase64, "image/png"));
      } else {
        message = UserMessage.from(
            ImageContent.from(screenshotBase64, "image/png"));
      }
    } else if (planningStrategy == PlanningStrategy.AUTO_GLM) {
      if (history.isEmpty()) {
        String promptText = "You are a professional Android operation agent assistant that can fulfill the user's high-level instructions. Given a screenshot of the Android interface at each step, you first analyze the situation, then plan the best course of action using Python-style pseudo-code.\n" +
            "\n" +
            "# More details about the code\n" +
            "Your response format must be structured as follows:\n" +
            "\n" +
            "Think first: Use <think>...</think> to analyze the current screen, identify key elements, and determine the most efficient action.\n" +
            "Provide the action: Use <answer>...</answer> to return a single line of pseudo-code representing the operation.\n" +
            "\n" +
            "Your output should STRICTLY follow the format:\n" +
            "<think>\n" +
            "[Your thought]\n" +
            "</think>\n" +
            "<answer>\n" +
            "[Your operation code]\n" +
            "</answer>\n" +
            "\n" +
            "- **Tap**\n" +
            "  Perform a tap action on a specified screen area. The element is a list of 2 integers, representing the coordinates of the tap point.\n" +
            "  **Example**:\n" +
            "  <answer>\n" +
            "  do(action=\"Tap\", element=[x,y])\n" +
            "  </answer>\n" +
            "- **Type**\n" +
            "  Enter text into the currently focused input field.\n" +
            "  **Example**:\n" +
            "  <answer>\n" +
            "  do(action=\"Type\", text=\"Hello World\")\n" +
            "  </answer>\n" +
            "- **Swipe**\n" +
            "  Perform a swipe action with start point and end point.\n" +
            "  **Examples**:\n" +
            "  <answer>\n" +
            "  do(action=\"Swipe\", start=[x1,y1], end=[x2,y2])\n" +
            "  </answer>\n" +
            "- **Long Press**\n" +
            "  Perform a long press action on a specified screen area.\n" +
            "  You can add the element to the action to specify the long press area. The element is a list of 2 integers, representing the coordinates of the long press point.\n" +
            "  **Example**:\n" +
            "  <answer>\n" +
            "  do(action=\"Long Press\", element=[x,y])\n" +
            "  </answer>\n" +
            "- **Launch**\n" +
            "  Launch an app. Try to use launch action when you need to launch an app. Check the instruction to choose the right app before you use this action.\n" +
            "  **Example**:\n" +
            "  <answer>\n" +
            "  do(action=\"Launch\", app=\"Settings\")\n" +
            "  </answer>\n" +
            "- **Back**\n" +
            "  Press the Back button to navigate to the previous screen.\n" +
            "  **Example**:\n" +
            "  <answer>\n" +
            "  do(action=\"Back\")\n" +
            "  </answer>\n" +
            "- **Finish**\n" +
            "  Terminate the program and optionally print a message.\n" +
            "  **Example**:\n" +
            "  <answer>\n" +
            "  finish(message=\"Task completed.\")\n" +
            "  </answer>\n" +
            "\n" +
            "\n" +
            "REMEMBER:\n" +
            "- Think before you act: Always analyze the current UI and the best course of action before executing any step, and output in <think> part.\n" +
            "- Only ONE LINE of action in <answer> part per response: Each step must contain exactly one line of executable code.\n" +
            "- Generate execution code strictly according to format requirements.\n" +
            "\n" +
            "## User Instruction\n" + instruction;
        message = UserMessage.from(
            TextContent.from(promptText),
            ImageContent.from(screenshotBase64, "image/png"));
      } else {
        message = UserMessage.from(
            ImageContent.from(screenshotBase64, "image/png"));
      }
    } else {
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
    }

    history.add(message);

    log.debug("Chat Plan message: {}", message);

    ChatResponse chatResponse = chatModel.chat(history);
    String responseJson = chatResponse.aiMessage().text();
    log.debug("AI Plan Response: {}", responseJson);
    history.add(AiMessage.from(responseJson));

    try {
      PlanningResponse planningResponse;
      if (planningStrategy == PlanningStrategy.UI_TARS) {
        int[] dims = CoordinateParser.getScreenshotDimensions(screenshotBase64);
        planningResponse = CoordinateParser.parseUiTarsResponse(responseJson, dims[0], dims[1]);
      } else if (planningStrategy == PlanningStrategy.AUTO_GLM) {
        int[] dims = CoordinateParser.getScreenshotDimensions(screenshotBase64);
        planningResponse = CoordinateParser.parseAutoGlmResponse(responseJson, dims[0], dims[1]);
      } else {
        planningResponse = parseXmlPlanningResponse(responseJson);
      }

      if (chatResponse.metadata() != null && chatResponse.metadata().tokenUsage() != null) {
        planningResponse.setDescription(chatResponse.metadata().tokenUsage().toString());
      }
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
