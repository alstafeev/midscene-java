package com.midscene.core.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an execution task in the reporting system. Mirrors the TypeScript ExecutionTask structure for better
 * report compatibility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionTask {

  /**
   * Unique task identifier.
   */
  private String taskId;

  /**
   * Task type (e.g., "Planning", "Insight", "Action Space", "Log").
   */
  private String type;

  /**
   * Sub-type for more specific categorization.
   */
  private String subType;

  /**
   * Whether this is a sub-task.
   */
  private Boolean subTask;

  /**
   * Task parameters.
   */
  private Map<String, Object> param;

  /**
   * AI's reasoning/thought process.
   */
  private String thought;

  /**
   * Task status (pending, running, finished, failed, cancelled).
   */
  private String status;

  /**
   * Task output data.
   */
  private Object output;

  /**
   * Task log data.
   */
  private Object log;

  /**
   * Error message if task failed.
   */
  private String errorMessage;

  /**
   * Error stack trace.
   */
  private String errorStack;

  /**
   * Timing information.
   */
  private TaskTiming timing;

  /**
   * AI usage statistics.
   */
  private AIUsageInfo usage;

  /**
   * Recorder items (screenshots, etc.).
   */
  private List<RecorderItem> recorder;
  /**
   * List of sub-goals for the current instruction.
   */
  private List<SubGoal> subGoals;

  private PageContext pageContext;


  /**
   * Reasoning content from AI.
   */
  @JsonProperty("reasoning_content")
  private String reasoningContent;

  /**
   * Represents a sub-goal in the planning process.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SubGoal {

    private Integer index;
    private String status; // pending, running, finished
    private String description;
    private List<String> logs;
  }

  /**
   * Task timing information. ...
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TaskTiming {

    private Long start;
    private Long end;
    private Long cost;
  }

  /**
   * AI usage information.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AIUsageInfo {

    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;

    @JsonProperty("time_cost")
    private Long timeCost;

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_description")
    private String modelDescription;

    private String intent;
  }

  /**
   * Recorder item (typically a screenshot).
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RecorderItem {

    private String type;
    private Long ts;
    private String screenshot;
  }
}
