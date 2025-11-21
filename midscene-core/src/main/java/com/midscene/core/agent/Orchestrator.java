package com.midscene.core.agent;

import com.midscene.core.model.AIModel;
import com.midscene.core.service.PageDriver;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Orchestrator {

  private static final Logger logger = LoggerFactory.getLogger(Orchestrator.class);
  private final PageDriver driver;
  private final Planner planner;
  private final Executor executor;

  public Orchestrator(PageDriver driver, AIModel aiModel) {
    this.driver = driver;
    this.planner = new Planner(aiModel);
    this.executor = new Executor(driver);
  }

  public String query(String question) {
    logger.info("Querying: {}", question);
    String screenshotBase64 = driver.getScreenshotBase64();
    return planner.query(question, screenshotBase64);
  }

  public void execute(String instruction) {
    logger.info("Executing instruction: {}", instruction);

    List<ChatMessage> history = new ArrayList<>();
    int maxRetries = 3;
    boolean finished = false;

    for (int i = 0; i < maxRetries && !finished; i++) {
      try {
        String screenshotBase64 = driver.getScreenshotBase64();

        PlanningResponse plan = planner.plan(instruction, screenshotBase64, history);

        if (plan.actions != null && !plan.actions.isEmpty()) {
          for (PlanningAction action : plan.actions) {
            executor.execute(action);
          }
          finished = true;
        } else {
          logger.warn("No actions returned by AI.");
          finished = true;
        }

      } catch (Exception e) {
        logger.error("Failed to execute plan (Attempt {})", i + 1, e);
        history.add(UserMessage.from("Error executing plan: " + e.getMessage()));
      }
    }

    if (!finished) {
      logger.error("Failed to complete instruction after {} attempts", maxRetries);
      throw new RuntimeException("Failed to complete instruction: " + instruction);
    }
  }
}
