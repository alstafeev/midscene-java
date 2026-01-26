package com.midscene.core.context;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an execution dump containing tasks and metadata. Mirrors the TypeScript IExecutionDump structure for
 * report compatibility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionDump {

  /**
   * Log timestamp.
   */
  private Long logTime;

  /**
   * Name of the execution (test name, instruction, etc.).
   */
  private String name;

  /**
   * Description of the execution.
   */
  private String description;

  /**
   * List of tasks executed.
   */
  @Builder.Default
  private List<ExecutionTask> tasks = new ArrayList<>();

  /**
   * AI action context string.
   */
  private String aiActContext;

  /**
   * Creates an ExecutionDump from a Context.
   *
   * @param context the context to convert
   * @param name    the name for the execution
   * @return an ExecutionDump
   */
  public static ExecutionDump fromContext(Context context, String name) {
    ExecutionDump dump = ExecutionDump.builder()
        .logTime(System.currentTimeMillis())
        .name(name)
        .tasks(new ArrayList<>())
        .build();

    for (ContextEvent event : context.getEvents()) {
      ExecutionTask task = eventToTask(event);
      dump.getTasks().add(task);
    }

    return dump;
  }

  private static ExecutionTask eventToTask(ContextEvent event) {
    ExecutionTask.ExecutionTaskBuilder builder = ExecutionTask.builder()
        .type(mapEventTypeToTaskType(event.getType()))
        .status("finished")
        .log(event.getData());

    // Add timing
    builder.timing(ExecutionTask.TaskTiming.builder()
        .start(event.getTimestamp())
        .end(event.getTimestamp())
        .cost(event.getDurationMs())
        .build());

    // Add usage info if available
    if (event.getTokensUsed() != null || event.getModelName() != null) {
      builder.usage(ExecutionTask.AIUsageInfo.builder()
          .totalTokens(event.getTokensUsed())
          .modelName(event.getModelName())
          .timeCostMs(event.getDurationMs())
          .build());
    }

    // Add thought/reasoning
    if (event.getThought() != null) {
      builder.thought(event.getThought());
    }

    // Add recorder for screenshots
    if (event.getScreenshotBase64() != null) {
      List<ExecutionTask.RecorderItem> recorder = new ArrayList<>();
      String base64 = event.getScreenshotBase64();
      if (!base64.startsWith("data:image")) {
        base64 = "data:image/png;base64," + base64;
      }
      recorder.add(ExecutionTask.RecorderItem.builder()
          .type("screenshot")
          .ts(event.getTimestamp())
          .screenshot(base64)
          .build());
      builder.recorder(recorder);
    }

    // Add error info
    if (event.getError() != null) {
      builder.status("failed");
      builder.error(event.getError());
    }

    // Add output
    if (event.getOutput() != null) {
      builder.output(event.getOutput());
    }

    return builder.build();
  }

  private static String mapEventTypeToTaskType(String eventType) {
    if (eventType == null) {
      return "Log";
    }
    return switch (eventType.toUpperCase()) {
      case "INSTRUCTION" -> "Planning";
      case "PLAN" -> "Planning";
      case "ACTION" -> "Action Space";
      case "QUERY", "EXTRACTION" -> "Insight";
      case "ASSERTION", "WAIT_FOR" -> "Insight";
      case "ERROR" -> "Log";
      case "SCREENSHOT_BEFORE", "SCREENSHOT_AFTER" -> "Log";
      default -> "Log";
    };
  }
}
