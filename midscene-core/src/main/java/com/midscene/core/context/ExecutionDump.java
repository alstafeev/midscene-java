package com.midscene.core.context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
   * Stable unique identifier for this execution run.
   */
  private String id;

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

  private PageContext pageContext;

  /**
   * Creates an ExecutionDump from a Context.
   *
   * @param context the context to convert
   * @param name    the name for the execution
   * @return an ExecutionDump
   */
  public static ExecutionDump fromContext(Context context, String name) {
    ExecutionDump dump = ExecutionDump.builder()
        .id(UUID.randomUUID().toString())
        .logTime(System.currentTimeMillis())
        .name(name)
        .tasks(new ArrayList<>())
        .build();

    int width = 1280;
    int height = 720;
    boolean sizeFound = false;

    // First pass: find dimensions from any screenshot
    for (ContextEvent event : context.getEvents()) {
      if (event.getScreenshotBase64() != null) {
        try {
          String base64 = event.getScreenshotBase64();
          String cleanBase64 = base64;
          if (cleanBase64.contains(",")) {
            cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
          }
          byte[] bytes = java.util.Base64.getDecoder().decode(cleanBase64.trim().replaceAll("\\s+", ""));
          try (var bis = new java.io.ByteArrayInputStream(bytes)) {
            var image = javax.imageio.ImageIO.read(bis);
            if (image != null) {
              width = image.getWidth();
              height = image.getHeight();
              sizeFound = true;
              break;
            }
          }
        } catch (Exception e) {
          // ignore
        }
      }
    }

    // Second pass: generate tasks with page context
    for (ContextEvent event : context.getEvents()) {
      ExecutionTask task = eventToTask(event, width, height);
      dump.getTasks().add(task);
    }

    dump.setPageContext(PageContext.builder()
        .size(Size.builder()
            .width(width)
            .height(height)
            .build())
        .build());

    return dump;
  }

  private static ExecutionTask eventToTask(ContextEvent event, int width, int height) {
    ExecutionTask.ExecutionTaskBuilder builder = ExecutionTask.builder()
        .pageContext(PageContext.builder()
            .size(Size.builder()
                .width(width)
                .height(height)
                .build())
            .build())
        .taskId(UUID.randomUUID().toString())
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
          .timeCost(event.getDurationMs())
          .build());
    }

    // Add thought/reasoning
    if (event.getThought() != null) {
      builder.thought(event.getThought());
    }

    // Add sub-goals
    if (event.getSubGoals() != null) {
      builder.subGoals(event.getSubGoals());
    }

    // Add recorder for screenshots
    if (event.getScreenshotBase64() != null) {
      List<ExecutionTask.RecorderItem> recorder = new ArrayList<>();
      String base64 = event.getScreenshotBase64();
      if (!base64.startsWith("data:image")) {
        base64 = "data:image/png;base64," + base64;
      }

      int imgWidth = width;
      int imgHeight = height;
      try {
        String cleanBase64 = base64;
        if (cleanBase64.contains(",")) {
          cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
        }
        byte[] bytes = java.util.Base64.getDecoder().decode(cleanBase64.trim().replaceAll("\\s+", ""));
        try (var bis = new java.io.ByteArrayInputStream(bytes)) {
          var image = javax.imageio.ImageIO.read(bis);
          if (image != null) {
            imgWidth = image.getWidth();
            imgHeight = image.getHeight();
          }
        }
      } catch (Exception e) {
        // use default fallback
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
      builder.errorMessage(event.getError());
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
