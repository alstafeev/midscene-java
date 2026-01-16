package com.midscene.core.context;

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
   * Error if task failed.
   */
  private String error;

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
   * Reasoning content from AI.
   */
  private String reasoningContent;

  /**
   * Task timing information.
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

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Long timeCostMs;
    private String modelName;
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
    private String timing;
  }
}
