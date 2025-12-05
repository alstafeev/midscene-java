package com.midscene.core.agent;

import com.midscene.core.config.MidsceneConfig;
import com.midscene.core.config.ModelProvider;
import com.midscene.core.model.AIModel;
import com.midscene.core.model.GeminiModel;
import com.midscene.core.model.OpenAIModel;
import com.midscene.core.service.PageDriver;
import java.util.concurrent.CompletableFuture;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Agent {

  private final Orchestrator orchestrator;

  public Agent(PageDriver driver, AIModel aiModel) {
    this.orchestrator = new Orchestrator(driver, aiModel);
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
      case ModelProvider.OPENAI -> new OpenAIModel(config.getApiKey(), config.getModelName());
      case ModelProvider.GEMINI -> new GeminiModel(config.getApiKey(), config.getModelName());
    };

    return new Agent(driver, model);
  }

  /**
   * Performs an AI-driven action on the page.
   *
   * @param instruction The instruction to execute
   */
  public void aiAction(String instruction) {
    orchestrator.execute(instruction);
  }

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
   * Performs an AI-driven action asynchronously.
   *
   * @param instruction The instruction to execute
   * @return A CompletableFuture representing the action execution
   */
  public CompletableFuture<Void> aiActionAsync(String instruction) {
    return CompletableFuture.runAsync(() -> aiAction(instruction));
  }

  /**
   * Gets the execution context.
   *
   * @return The execution context
   */
  public com.midscene.core.context.Context getContext() {
    return orchestrator.getContext();
  }
}
