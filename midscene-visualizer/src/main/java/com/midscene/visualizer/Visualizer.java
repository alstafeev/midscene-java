package com.midscene.visualizer;

import com.exasol.mavenprojectversiongetter.MavenProjectVersionGetter;
import com.midscene.core.context.Context;
import com.midscene.core.context.ContextEvent;
import com.midscene.core.utils.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Visualizer {

  public static void generateReport(Context context, Path outputPath) {
    log.info("Generating report to {}", outputPath);
    try {
      Map<String, Object> execution = getTaskList(context);

      Map<String, Object> grouped = new HashMap<>();
      grouped.put("executions", List.of(execution));

      String dumpJson = ObjectMapper.writeValueAsString(grouped);

      MidsceneReportGenerator generator = new MidsceneReportGenerator();
      generator.generateReport(dumpJson, null, outputPath);

      log.info("Report generated successfully.");
    } catch (IOException e) {
      log.error("Failed to write report to {}", outputPath, e);
      throw new RuntimeException("Failed to generate report", e);
    }
  }

  private static Map<String, Object> getTaskList(Context context) {
    List<Map<String, Object>> tasks = new ArrayList<>();
    for (ContextEvent event : context.getEvents()) {
      Map<String, Object> task = new HashMap<>();
      task.put("type", "gametask");
      task.put("name", event.getDescription());
      task.put("status", "finished");
      task.put("ts", event.getTimestamp());
      task.put("log", event.getData());

      Map<String, Object> log = new HashMap<>();
      log.put("data", event.getData());
      if (event.getScreenshotBase64() != null) {
        task.put("img", event.getScreenshotBase64());
      }

      tasks.add(task);
    }

    Map<String, Object> execution = new HashMap<>();
    execution.put("tasks", tasks);
    execution.put("sdkVersion", MavenProjectVersionGetter.getCurrentProjectVersion());
    return execution;
  }
}
