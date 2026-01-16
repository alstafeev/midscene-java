package com.midscene.core.pojo.planning;

import java.util.List;
import lombok.Data;

/**
 * Response object from AI planning containing actions to execute. Matches the TypeScript version's planning response
 * structure.
 */
@Data
public class PlanningResponse {

  /**
   * List of actions to execute.
   */
  private List<ActionsItem> actions;

  /**
   * Brief description of what the AI is about to do. This is the "log" field from the AI response.
   */
  private String log;

  /**
   * Indicates if more actions are needed to complete the instruction.
   */
  private Boolean moreActionsNeededByInstruction;

  /**
   * Milliseconds to wait after executing the actions.
   */
  private Integer sleep;

  /**
   * Error message if the AI cannot proceed.
   */
  private String error;

  /**
   * Token usage description (set by Java code, not from AI).
   */
  private String description;
}