package com.midscene.core.agent;

import com.midscene.core.cache.TaskCache;
import com.midscene.core.context.Context;
import com.midscene.core.context.ExecutionTask;
import dev.langchain4j.model.chat.ChatModel;
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

  public Orchestrator(PageDriver driver, ChatModel chatModel) {
    this(driver, new Planner(chatModel, TaskCache.disabled()), new Executor(driver), 3);
  }

  public Orchestrator(PageDriver driver, ChatModel chatModel, TaskCache cache) {
    this(driver, new Planner(chatModel, cache), new Executor(driver), 3);
  }

  public Orchestrator(PageDriver driver, ChatModel chatModel, TaskCache cache, int maxRetries) {
    this(driver, new Planner(chatModel, cache), new Executor(driver), maxRetries);
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

  public com.midscene.core.pojo.response.AssertionAiResponse assertCondition(String assertion) {
    log.info("Asserting: {}", assertion);
    context.logInstruction("Assert: " + assertion);

    String screenshotBase64 = driver.getScreenshotBase64();
    context.logScreenshotBefore(screenshotBase64);

    com.midscene.core.pojo.response.AssertionAiResponse response = planner.assertCondition(assertion, screenshotBase64);
    
    // Log structured assertion result
    context.logAction("Assertion " + (response.isPass() ? "PASSED" : "FAILED") + ": " + response.getThought());

    return response;
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
    String sessionMemory = null;
    List<ExecutionTask.SubGoal> currentSubGoals = new ArrayList<>();

    for (int i = 0; i < maxRetries && !finished; i++) {
      try {
        String screenshotBase64 = driver.getScreenshotBase64();
        context.logScreenshotBefore(screenshotBase64);

        String pageSource = driver.getDomSnapshot();
        // Fallback to getPageSource if dom extractor fails or returns empty
        if (pageSource == null || pageSource.equals("[]")) {
            pageSource = driver.getPageSource();
        }

        StringBuilder instructionBuilder = new StringBuilder(instruction);
        if (sessionMemory != null) {
            instructionBuilder.append("\n\nSession Memory: ").append(sessionMemory);
        }
        if (!currentSubGoals.isEmpty()) {
            instructionBuilder.append("\n\nCurrent Sub-goals status:\n");
            for (ExecutionTask.SubGoal sg : currentSubGoals) {
                instructionBuilder.append("- ").append(sg.getIndex()).append(". ")
                    .append(sg.getDescription()).append(" (").append(sg.getStatus()).append(")\n");
            }
        }

        PlanningResponse plan = planner.plan(instructionBuilder.toString(), screenshotBase64, pageSource, history);
        
        if (plan.getThought() != null) {
            log.info("AI Thought: {}", plan.getThought());
        }
        if (plan.getLog() != null) {
            log.info("AI Log: {}", plan.getLog());
        }

        // Update local sub-goals state
        if (plan.getUpdateSubGoals() != null) {
            currentSubGoals = plan.getUpdateSubGoals();
        }
        if (plan.getMarkFinishedIndexes() != null) {
            for (Integer index : plan.getMarkFinishedIndexes()) {
                currentSubGoals.stream()
                    .filter(sg -> sg.getIndex().equals(index))
                    .findFirst()
                    .ifPresent(sg -> sg.setStatus("finished"));
            }
        }

        context.logPlan(plan.getThought(), new ArrayList<>(currentSubGoals), plan.getRawResponse());
        
        if (plan.getMemory() != null) {
            sessionMemory = plan.getMemory();
        }
        
        if (plan.getOutput() != null || Boolean.TRUE.equals(plan.getFinalizeSuccess())) {
          log.info("Task completed: {}", plan.getOutput());
          context.logAction("Task completed: " + plan.getOutput());
          finished = true;
          break;
        }

        if (Objects.nonNull(plan.getActions()) && !plan.getActions().isEmpty()) {
          for (ActionsItem action : plan.getActions()) {
            executor.execute(action);
          }
          context.logScreenshotAfter(driver.getScreenshotBase64());
          // If model didn't explicitly say it's complete, but actions array is empty 
          // or moreActionsNeeded is false (if we still used it), we might want to check here.
          // But with Planning 2.0, we rely on <complete> tag.
        } else if (plan.getError() != null) {
          throw new RuntimeException(plan.getError());
        } else {
          // If no actions and no completion signal, it might be an implicit completion or failure
          log.warn("AI returned no actions and no completion signal.");
          finished = true; 
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
