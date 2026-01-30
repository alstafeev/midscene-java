package com.midscene.core.agent;

import com.midscene.core.cache.TaskCache;
import com.midscene.core.context.Context;
import com.midscene.core.model.AIModel;
import com.midscene.core.pojo.planning.ActionsItem;
import com.midscene.core.pojo.planning.PlanningResponse;
import com.midscene.core.service.PageDriver;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Orchestrator {

  private final PageDriver driver;
  private final Planner planner;
  private final Executor executor;
  private final int maxRetries;
  @Getter
  private final Context context;

  public Orchestrator(PageDriver driver, AIModel aiModel) {
    this(driver, new Planner(aiModel, TaskCache.disabled()), new Executor(driver), 3);
  }

  public Orchestrator(PageDriver driver, AIModel aiModel, TaskCache cache) {
    this(driver, new Planner(aiModel, cache), new Executor(driver), 3);
  }

  public Orchestrator(PageDriver driver, AIModel aiModel, TaskCache cache, int maxRetries) {
    this(driver, new Planner(aiModel, cache), new Executor(driver), maxRetries);
  }

  /**
   * Constructor for testing purposes.
   *
   * @param driver   The page driver
   * @param planner  The planner
   * @param executor The executor
   */
  protected Orchestrator(PageDriver driver, Planner planner, Executor executor) {
    this(driver, planner, executor, 3);
  }

  protected Orchestrator(PageDriver driver, Planner planner, Executor executor, int maxRetries) {
    this.driver = driver;
    this.planner = planner;
    this.executor = executor;
    this.maxRetries = maxRetries;
    this.context = new Context();
  }

  /**
   * Queries the page for information using the AI model.
   *
   * @param question The question to ask about the page
   * @return The answer from the AI
   */
  public String query(String question) {
    log.info("Querying: {}", question);
    context.logInstruction("Query: " + question);

    String screenshotBase64 = driver.getScreenshotBase64();
    context.logScreenshotBefore(screenshotBase64);

    String answer = planner.query(question, screenshotBase64);
    context.logAction("Answer: " + answer);

    return answer;
  }

  /**
   * Executes a natural language instruction on the page.
   *
   * @param instruction The instruction to execute
   * @throws RuntimeException if the instruction fails to execute after retries
   */
  public void execute(String instruction) {
    log.info("Executing instruction: {}", instruction);
    context.logInstruction(instruction);

    List<ChatMessage> history = new ArrayList<>();
    boolean finished = false;
    boolean cacheInvalidated = false;

    for (int i = 0; i < maxRetries && !finished; i++) {
      try {
        String screenshotBase64 = driver.getScreenshotBase64();
        context.logScreenshotBefore(screenshotBase64);

        String pageSource = driver.getPageSource();

        PlanningResponse plan = planner.plan(instruction, screenshotBase64, pageSource, history);
        context.logPlan(plan.toString());
        context.logAction("Token usage: " + plan.getDescription());

        if (Objects.nonNull(plan.getActions()) && !plan.getActions().isEmpty()) {
          for (ActionsItem action : plan.getActions()) {
            executor.execute(action);
          }
          context.logScreenshotAfter(driver.getScreenshotBase64());
          finished = true;
        } else {
          throw new RuntimeException("No actions returned by AI.");
        }

      } catch (Exception e) {
        log.error("Failed to execute plan (Attempt {}) {}", i + 1, e.getMessage());
        context.logError("Attempt " + (i + 1) + " failed: " + e.getMessage());
        
        // On first failure, invalidate cache and clear history to force fresh AI call
        if (!cacheInvalidated && i == 0) {
          boolean wasInvalidated = planner.invalidateCache(instruction);
          if (wasInvalidated) {
            log.info("Invalidated stale cache for instruction: {}", instruction);
            history.clear(); // Clear history to get fresh plan from AI
            cacheInvalidated = true;
          }
        }
        
        history.add(UserMessage.from("Error executing plan: " + e.getMessage()));
      }
    }

    if (!finished) {
      log.error("Failed to complete instruction after {} attempts", maxRetries);
      context.logError("Failed to complete instruction: " + instruction);
      throw new RuntimeException("Failed to complete instruction: " + instruction);
    }
  }
}
