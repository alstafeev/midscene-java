package com.midscene.core.pojo.planning;

import com.midscene.core.context.ExecutionTask;
import java.util.List;
import lombok.Data;

/**
 * Response object from AI planning containing actions to execute. Matches the TypeScript version's planning response
 * structure (Planning 2.0).
 */
@Data
public class PlanningResponse {

  /**
   * AI's reasoning process.
   */
  private String thought;

  /**
   * Brief description of what the AI is about to do.
   */
  private String log;

  /**
   * List of actions to execute.
   */
  private List<ActionsItem> actions;

  /**
   * Updated sub-goals from AI.
   */
  private List<ExecutionTask.SubGoal> updateSubGoals;

  /**
   * Indexes of sub-goals to mark as finished.
   */
  private List<Integer> markFinishedIndexes;

  /**
   * Final summary message for the user if task is complete.
   */
  private String output;

  /**
   * Whether the task was successfully completed.
   */
  private Boolean finalizeSuccess;

  /**
   * Error message if the AI cannot proceed.
   */
  private String error;

  /**
   * Token usage description (set by Java code, not from AI).
   */
  private String description;
/**
 * Memory data to persist between steps.
 */
private String memory;

/**
 * Raw XML response from AI.
...
   */
  private String rawResponse;
}
