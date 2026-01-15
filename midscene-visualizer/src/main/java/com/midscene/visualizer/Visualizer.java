package com.midscene.visualizer;

import com.exasol.mavenprojectversiongetter.MavenProjectVersionGetter;
import com.midscene.core.context.Context;
import com.midscene.core.context.ExecutionDump;
import com.midscene.core.context.ExecutionTask;
import com.midscene.core.context.GroupedActionDump;
import com.midscene.core.utils.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.log4j.Log4j2;

/**
 * Generates HTML reports from execution contexts. Uses the enhanced ExecutionDump structure for better compatibility
 * with the report UI.
 */
@Log4j2
public class Visualizer {

  /**
   * Generates an HTML report from a Context.
   *
   * @param context    the execution context
   * @param outputPath the output file path
   */
  public static void generateReport(Context context, Path outputPath) {
    generateReport(context, outputPath, "Test Execution");
  }

  /**
   * Generates an HTML report from a Context with a custom name.
   *
   * @param context    the execution context
   * @param outputPath the output file path
   * @param name       the execution name
   */
  public static void generateReport(Context context, Path outputPath, String name) {
    log.info("Generating report to {}", outputPath);
    try {
      String sdkVersion = MavenProjectVersionGetter.getCurrentProjectVersion();
      GroupedActionDump dump = GroupedActionDump.fromContext(context, name, sdkVersion);

      // Add model briefs from events
      for (ExecutionDump execution : dump.getExecutions()) {
        for (ExecutionTask task : execution.getTasks()) {
          if (task.getUsage() != null && task.getUsage().getModelName() != null) {
            dump.addModelBrief(task.getUsage().getModelName());
          }
        }
      }

      String dumpJson = ObjectMapper.writeValueAsString(dump);

      MidsceneReportGenerator generator = new MidsceneReportGenerator();
      generator.generateReport(dumpJson, null, outputPath);

      log.info("Report generated successfully.");
    } catch (IOException e) {
      log.error("Failed to write report to {}", outputPath, e);
      throw new RuntimeException("Failed to generate report", e);
    }
  }

  /**
   * Generates an HTML report from a GroupedActionDump.
   *
   * @param dump       the grouped action dump
   * @param outputPath the output file path
   */
  public static void generateReport(GroupedActionDump dump, Path outputPath) {
    log.info("Generating report from GroupedActionDump to {}", outputPath);
    try {
      String dumpJson = ObjectMapper.writeValueAsString(dump);

      MidsceneReportGenerator generator = new MidsceneReportGenerator();
      generator.generateReport(dumpJson, null, outputPath);

      log.info("Report generated successfully.");
    } catch (IOException e) {
      log.error("Failed to write report to {}", outputPath, e);
      throw new RuntimeException("Failed to generate report", e);
    }
  }

  /**
   * Generates an HTML report from multiple Contexts.
   *
   * @param contexts   list of contexts with their names
   * @param groupName  the group name for the report
   * @param outputPath the output file path
   */
  public static void generateReport(List<NamedContext> contexts, String groupName, Path outputPath) {
    log.info("Generating grouped report to {}", outputPath);
    try {
      String sdkVersion = MavenProjectVersionGetter.getCurrentProjectVersion();

      GroupedActionDump dump = GroupedActionDump.builder()
          .sdkVersion(sdkVersion)
          .groupName(groupName)
          .build();

      for (NamedContext nc : contexts) {
        ExecutionDump execution = ExecutionDump.fromContext(nc.context(), nc.name());
        dump.getExecutions().add(execution);

        // Collect model briefs
        for (ExecutionTask task : execution.getTasks()) {
          if (task.getUsage() != null && task.getUsage().getModelName() != null) {
            dump.addModelBrief(task.getUsage().getModelName());
          }
        }
      }

      String dumpJson = ObjectMapper.writeValueAsString(dump);

      MidsceneReportGenerator generator = new MidsceneReportGenerator();
      generator.generateReport(dumpJson, null, outputPath);

      log.info("Report generated successfully.");
    } catch (IOException e) {
      log.error("Failed to write report to {}", outputPath, e);
      throw new RuntimeException("Failed to generate report", e);
    }
  }

  /**
   * Record for holding a context with its name.
   */
  public record NamedContext(Context context, String name) {

  }
}
