package com.midscene.core.yaml;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a script execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptResult {

  /**
   * Path to the script file.
   */
  private String scriptPath;

  /**
   * Whether the script executed successfully.
   */
  private boolean success;

  /**
   * Whether the script was executed.
   */
  private boolean executed;

  /**
   * Error message if execution failed.
   */
  private String error;

  /**
   * Duration of execution in milliseconds.
   */
  private long durationMs;

  /**
   * List of task statuses.
   */
  private java.util.List<TaskStatus> taskStatuses;

  /**
   * Map of query results by name.
   */
  private Map<String, Object> queryResults;

  /**
   * Path to the generated report.
   */
  private String reportPath;

  /**
   * Result type.
   */
  private ResultType resultType;

  /**
   * Creates a successful result.
   *
   * @param scriptPath the script path
   * @param durationMs the duration
   * @return a success result
   */
  public static ScriptResult success(String scriptPath, long durationMs) {
    return ScriptResult.builder()
        .scriptPath(scriptPath)
        .success(true)
        .executed(true)
        .durationMs(durationMs)
        .resultType(ResultType.SUCCESS)
        .build();
  }

  /**
   * Creates a failed result.
   *
   * @param scriptPath the script path
   * @param error      the error message
   * @param durationMs the duration
   * @return a failed result
   */
  public static ScriptResult failed(String scriptPath, String error, long durationMs) {
    return ScriptResult.builder()
        .scriptPath(scriptPath)
        .success(false)
        .executed(true)
        .error(error)
        .durationMs(durationMs)
        .resultType(ResultType.FAILED)
        .build();
  }

  /**
   * Types of execution results.
   */
  public enum ResultType {
    SUCCESS,
    FAILED,
    PARTIAL_FAILED,
    NOT_EXECUTED
  }
}
