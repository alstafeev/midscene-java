package com.midscene.core.agent;

import com.midscene.core.cache.TaskCache;
import com.midscene.core.config.MidsceneConfig;
import com.midscene.core.context.Context;
import com.midscene.core.model.AIModel;
import com.midscene.core.model.AnthropicModel;
import com.midscene.core.model.AzureOpenAiModel;
import com.midscene.core.model.GeminiModel;
import com.midscene.core.model.MistralModel;
import com.midscene.core.model.OllamaModel;
import com.midscene.core.model.OpenAIModel;
import com.midscene.core.model.QwenModel;
import com.midscene.core.pojo.options.InputOptions;
import com.midscene.core.pojo.options.LocateOptions;
import com.midscene.core.pojo.options.ScrollOptions;
import com.midscene.core.pojo.options.WaitOptions;
import com.midscene.core.service.PageDriver;
import java.util.concurrent.CompletableFuture;
import lombok.extern.log4j.Log4j2;

/**
 * The main entry point for AI-powered browser automation. Provides natural language methods for interacting with web
 * pages.
 */
@Log4j2
public class Agent {

  private static final String INIT_MUTATION_OBSERVER_SCRIPT = """
      window.__midscene_mutation_happened = true; // Set to true initially to ensure first check
      if (!window.__midscene_observer) {
        window.__midscene_observer = new MutationObserver(() => {
          window.__midscene_mutation_happened = true;
        });
        window.__midscene_observer.observe(document.body, { childList: true, subtree: true, attributes: true, characterData: true });
      }
      """;

  private static final String CHECK_AND_RESET_MUTATION_SCRIPT = """
      var happened = window.__midscene_mutation_happened;
      window.__midscene_mutation_happened = false;
      return happened;
      """;

  private final Orchestrator orchestrator;
  private final PageDriver driver;
  private TaskCache cache;

  public Agent(PageDriver driver, AIModel aiModel) {
    this(driver, aiModel, TaskCache.disabled(), 3);
  }

  public Agent(PageDriver driver, AIModel aiModel, TaskCache cache) {
    this(driver, aiModel, cache, 3);
  }

  public Agent(PageDriver driver, AIModel aiModel, TaskCache cache, int maxRetries) {
    this.driver = driver;
    this.cache = cache != null ? cache : TaskCache.disabled();
    this.orchestrator = new Orchestrator(driver, aiModel, this.cache, maxRetries);
  }

  /**
   * Creates a new Agent instance with the given configuration and driver.
   *
   * @param config The configuration for the agent
   * @param driver The page driver
   * @return A new Agent instance
   */
  public static Agent create(MidsceneConfig config, PageDriver driver) {
    AIModel model = switch (config.getProvider()) {
      case OPENAI -> new OpenAIModel(config.getApiKey(), config.getModelName());
      case GEMINI -> new GeminiModel(config.getApiKey(), config.getModelName());
      case ANTHROPIC -> new AnthropicModel(config.getApiKey(), config.getModelName(), config.getBaseUrl());
      case MISTRAL -> new MistralModel(config.getApiKey(), config.getModelName(), config.getBaseUrl());
      case AZURE_OPEN_AI -> new AzureOpenAiModel(config.getApiKey(), config.getBaseUrl());
      case OLLAMA -> new OllamaModel(config.getBaseUrl(), config.getModelName());
      case QWEN, THOUSAND_QUESTIONS -> new QwenModel(config.getApiKey(), config.getModelName(), config.getBaseUrl());
    };

    return new Agent(driver, model, TaskCache.disabled(), config.getMaxRetries());
  }

  /**
   * Creates a new Agent instance with the given configuration, driver, and cache.
   *
   * @param config The configuration for the agent
   * @param driver The page driver
   * @param cache  The task cache for storing plans
   * @return A new Agent instance
   */
  public static Agent create(MidsceneConfig config, PageDriver driver, TaskCache cache) {
    AIModel model = switch (config.getProvider()) {
      case OPENAI -> new OpenAIModel(config.getApiKey(), config.getModelName());
      case GEMINI -> new GeminiModel(config.getApiKey(), config.getModelName());
      case ANTHROPIC -> new AnthropicModel(config.getApiKey(), config.getModelName(), config.getBaseUrl());
      case MISTRAL -> new MistralModel(config.getApiKey(), config.getModelName(), config.getBaseUrl());
      case AZURE_OPEN_AI -> new AzureOpenAiModel(config.getApiKey(), config.getBaseUrl());
      case OLLAMA -> new OllamaModel(config.getBaseUrl(), config.getModelName());
      case QWEN, THOUSAND_QUESTIONS -> new QwenModel(config.getApiKey(), config.getModelName(), config.getBaseUrl());
    };

    return new Agent(driver, model, cache, config.getMaxRetries());
  }

  /**
   * Performs an AI-driven action on the page using natural language.
   *
   * @param instruction The instruction to execute
   */
  public void aiAction(String instruction) {
    this.orchestrator.execute(instruction);
  }

  /**
   * Alias for aiAction. Performs an AI-driven action on the page.
   *
   * @param instruction The instruction to execute
   */
  public void aiAct(String instruction) {
    aiAction(instruction);
  }

  /**
   * Tap on an element described by natural language.
   *
   * @param locatePrompt Description of the element to tap
   */
  public void aiTap(String locatePrompt) {
    aiTap(locatePrompt, null);
  }

  /**
   * Tap on an element with options.
   *
   * @param locatePrompt Description of the element to tap
   * @param options      Locate options
   */
  public void aiTap(String locatePrompt, LocateOptions options) {
    runAction("Tap", locatePrompt, options);
  }

  /**
   * Double-click on an element described by natural language.
   *
   * @param locatePrompt Description of the element to double-click
   */
  public void aiDoubleClick(String locatePrompt) {
    aiDoubleClick(locatePrompt, null);
  }

  /**
   * Double-click on an element with options.
   *
   * @param locatePrompt Description of the element to double-click
   * @param options      Locate options
   */
  public void aiDoubleClick(String locatePrompt, LocateOptions options) {
    runAction("Double Click", locatePrompt, options);
  }

  /**
   * Right-click on an element described by natural language.
   *
   * @param locatePrompt Description of the element to right-click
   */
  public void aiRightClick(String locatePrompt) {
    aiRightClick(locatePrompt, null);
  }

  /**
   * Right-click on an element with options.
   *
   * @param locatePrompt Description of the element to right-click
   * @param options      Locate options
   */
  public void aiRightClick(String locatePrompt, LocateOptions options) {
    runAction("Right Click", locatePrompt, options);
  }

  /**
   * Hover over an element described by natural language.
   *
   * @param locatePrompt Description of the element to hover over
   */
  public void aiHover(String locatePrompt) {
    aiHover(locatePrompt, null);
  }

  /**
   * Hover over an element with options.
   *
   * @param locatePrompt Description of the element to hover over
   * @param options      Locate options
   */
  public void aiHover(String locatePrompt, LocateOptions options) {
    runAction("Hover", locatePrompt, options);
  }

  /**
   * Input text into an element described by natural language.
   *
   * @param locatePrompt Description of the input element
   * @param value        The value to input
   */
  public void aiInput(String locatePrompt, String value) {
    aiInput(locatePrompt, InputOptions.builder().value(value).build());
  }

  /**
   * Input text into an element with options.
   *
   * @param locatePrompt Description of the input element
   * @param options      Input options including value and mode
   */
  public void aiInput(String locatePrompt, InputOptions options) {
    String value = options.getValue() != null ? options.getValue() : "";
    String modeStr = "";
    if (options.getMode() != null) {
      switch (options.getMode()) {
        case APPEND -> modeStr = " (append to existing text)";
        case CLEAR -> modeStr = " (clear the field)";
        default -> modeStr = "";
      }
    }
    aiAction("Type '" + value + "' into " + locatePrompt + modeStr);
  }

  /**
   * Press a keyboard key.
   *
   * @param keyName The name of the key to press (e.g., "Enter", "Escape", "Tab")
   */
  public void aiKeyboardPress(String keyName) {
    aiKeyboardPress(null, keyName);
  }

  /**
   * Press a keyboard key on a specific element.
   *
   * @param locatePrompt Description of the element to focus before pressing the key
   * @param keyName      The name of the key to press
   */
  public void aiKeyboardPress(String locatePrompt, String keyName) {
    if (locatePrompt != null) {
      aiAction("Press '" + keyName + "' on " + locatePrompt);
    } else {
      aiAction("Press '" + keyName + "'");
    }
  }

  // ========== Scroll Actions ==========

  /**
   * Scroll on the page with the given options.
   *
   * @param options Scroll options including direction and type
   */
  public void aiScroll(ScrollOptions options) {
    aiScroll(null, options);
  }

  /**
   * Scroll on a specific element with options.
   *
   * @param locatePrompt Description of the element to scroll (null for page scroll)
   * @param options      Scroll options
   */
  public void aiScroll(String locatePrompt, ScrollOptions options) {
    StringBuilder instruction = new StringBuilder("Scroll ");

    if (options.getDirection() != null) {
      instruction.append(options.getDirection().name().toLowerCase()).append(" ");
    } else {
      instruction.append("down ");
    }

    if (locatePrompt != null && !locatePrompt.isEmpty()) {
      instruction.append("on ").append(locatePrompt).append(" ");
    }

    if (options.getScrollType() != null) {
      switch (options.getScrollType()) {
        case SCROLL_TO_TOP -> instruction.append("until reaching the top");
        case SCROLL_TO_BOTTOM -> instruction.append("until reaching the bottom");
        case SCROLL_TO_LEFT -> instruction.append("until reaching the left edge");
        case SCROLL_TO_RIGHT -> instruction.append("until reaching the right edge");
        default -> {
        }
      }
    }

    if (options.getDistance() != null) {
      instruction.append("by ").append(options.getDistance()).append(" pixels");
    }

    aiAction(instruction.toString().trim());
  }

  // ========== Query Actions ==========

  /**
   * Queries the page for information using the AI.
   *
   * @param question The question to ask about the page
   * @return The answer from the AI
   */
  public String aiQuery(String question) {
    return orchestrator.query(question);
  }

  /**
   * Query the page and get a boolean result.
   *
   * @param prompt The question to evaluate as true/false
   * @return true or false based on the AI's evaluation
   */
  public boolean aiBoolean(String prompt) {
    String answer = aiQuery(prompt + " Answer with only 'true' or 'false'.");
    return answer.toLowerCase().contains("true");
  }

  /**
   * Query the page and get a numeric result.
   *
   * @param prompt The question expecting a numeric answer
   * @return The numeric value extracted from the AI's response
   */
  public double aiNumber(String prompt) {
    String answer = aiQuery(prompt + " Answer with only a number.");
    try {
      return Double.parseDouble(answer.replaceAll("[^0-9.-]", ""));
    } catch (NumberFormatException e) {
      log.warn("Failed to parse number from AI response: {}", answer);
      return 0.0;
    }
  }

  /**
   * Query the page and get a string result.
   *
   * @param prompt The question expecting a text answer
   * @return The text answer from the AI
   */
  public String aiString(String prompt) {
    return aiQuery(prompt);
  }

  // ========== Assertion Actions ==========

  /**
   * Assert a condition on the page.
   *
   * @param assertion The assertion to verify
   * @throws AssertionError if the assertion fails
   */
  public void aiAssert(String assertion) {
    boolean result = aiBoolean("Is the following true? " + assertion);
    if (!result) {
      throw new AssertionError("AI Assertion failed: " + assertion);
    }
    log.info("AI Assertion passed: {}", assertion);
  }

  /**
   * Wait for a condition to become true.
   *
   * @param assertion The condition to wait for
   * @param options   Wait options including timeout
   */
  public void aiWaitFor(String assertion, WaitOptions options) {
    long timeoutMs = options.getTimeoutMs();
    long checkIntervalMs = options.getCheckIntervalMs();
    long startTime = System.currentTimeMillis();

    // Initialize MutationObserver
    try {
      driver.executeScript(INIT_MUTATION_OBSERVER_SCRIPT);
    } catch (Exception e) {
      log.warn("Failed to inject MutationObserver, falling back to polling: {}", e.getMessage());
    }

    while (System.currentTimeMillis() - startTime < timeoutMs) {
      boolean shouldCheck = true;
      try {
        Object result = driver.executeScript(CHECK_AND_RESET_MUTATION_SCRIPT);
        if (result instanceof Boolean) {
          shouldCheck = (Boolean) result;
        }
      } catch (Exception e) {
        // Fallback to true if script fails
        shouldCheck = true;
      }

      if (shouldCheck) {
        boolean result = aiBoolean("Is the following currently true? " + assertion);
        if (result) {
          log.info("Wait condition satisfied: {}", assertion);
          return;
        }
      }

      try {
        Thread.sleep(checkIntervalMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Wait interrupted", e);
      }
    }

    if (options.isThrowOnTimeout()) {
      throw new RuntimeException("Wait timeout: " + assertion);
    }
    log.warn("Wait timed out: {}", assertion);
  }

  /**
   * Wait for a condition with default options (30s timeout).
   *
   * @param assertion The condition to wait for
   */
  public void aiWaitFor(String assertion) {
    aiWaitFor(assertion, WaitOptions.builder().build());
  }

  // ========== Async Methods ==========

  /**
   * Performs an AI-driven action asynchronously.
   *
   * @param instruction The instruction to execute
   * @return A CompletableFuture representing the action execution
   */
  public CompletableFuture<Void> aiActionAsync(String instruction) {
    return CompletableFuture.runAsync(() -> aiAction(instruction));
  }

  // ========== Utility Methods ==========

  /**
   * Gets the execution context.
   *
   * @return The execution context
   */
  public Context getContext() {
    return orchestrator.getContext();
  }

  /**
   * Gets the underlying page driver.
   *
   * @return The page driver
   */
  public PageDriver getDriver() {
    return driver;
  }

  /**
   * Gets the task cache.
   *
   * @return The task cache
   */
  public TaskCache getCache() {
    return cache;
  }

  /**
   * Sets the task cache.
   *
   * @param cache The task cache to use
   */
  public void setCache(TaskCache cache) {
    this.cache = cache != null ? cache : TaskCache.disabled();
  }

  // ========== Private Helper Methods ==========

  private void runAction(String action, String locatePrompt, LocateOptions options) {
    String instruction = buildLocateInstruction(action, locatePrompt, options);
    aiAction(instruction);
  }

  private String buildLocateInstruction(String action, String locatePrompt, LocateOptions options) {
    StringBuilder instruction = new StringBuilder(action).append(" ").append(locatePrompt);

    if (options != null) {
      if (options.getSearchAreaPrompt() != null) {
        instruction.append(" within ").append(options.getSearchAreaPrompt());
      }
      if (Boolean.TRUE.equals(options.getDeepThink())) {
        instruction.append(" (use careful analysis)");
      }
    }

    return instruction.toString();
  }
}
