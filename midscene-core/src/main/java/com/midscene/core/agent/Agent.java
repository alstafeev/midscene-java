package com.midscene.core.agent;

import com.midscene.core.config.MidsceneConfig;
import com.midscene.core.config.ModelProvider;
import com.midscene.core.model.AIModel;
import com.midscene.core.model.GeminiModel;
import com.midscene.core.model.OpenAIModel;
import com.midscene.core.service.PageDriver;
import java.util.concurrent.CompletableFuture;

public class Agent {

  private final PageDriver driver;
  private final AIModel aiModel;
  private final Orchestrator orchestrator;

  public Agent(PageDriver driver, AIModel aiModel) {
    this.driver = driver;
    this.aiModel = aiModel;
    this.orchestrator = new Orchestrator(driver, aiModel);
  }

  public static Agent create(MidsceneConfig config, PageDriver driver) {
    AIModel model;
    if (config.getProvider() == ModelProvider.OPENAI) {
      model = new OpenAIModel(config.getApiKey(), config.getModelName());
    } else if (config.getProvider() == ModelProvider.GEMINI) {
      model = new GeminiModel(config.getApiKey(), config.getModelName());
    } else {
      throw new IllegalArgumentException("Unsupported provider: " + config.getProvider());
    }
    return new Agent(driver, model);
  }

  public void aiAction(String instruction) {
    orchestrator.execute(instruction);
  }

  public String aiQuery(String question) {
    return orchestrator.query(question);
  }

  public CompletableFuture<Void> aiActionAsync(String instruction) {
    return CompletableFuture.runAsync(() -> aiAction(instruction));
  }
}
