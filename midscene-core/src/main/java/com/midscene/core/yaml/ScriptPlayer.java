package com.midscene.core.yaml;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.midscene.core.agent.Agent;
import com.midscene.core.cache.TaskCache;
import com.midscene.core.pojo.options.WaitOptions;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Executes Midscene YAML scripts. Parses YAML files and runs the defined tasks and flow items.
 */
@Log4j2
public class ScriptPlayer {

  private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Getter
  private final Agent agent;

  @Getter
  private final MidsceneYamlScript script;

  @Getter
  private final String scriptPath;

  // Use LinkedHashMap to preserve order of tasks while allowing O(1) lookup
  private final Map<String, TaskStatus> statusMap = new LinkedHashMap<>();

  @Getter
  private final Map<String, Object> results = new HashMap<>();

  @Setter
  private Consumer<TaskStatus> statusListener;

  /**
   * Creates a ScriptPlayer from a YAML file.
   *
   * @param scriptPath path to the YAML script file
   * @param agent      the agent to use for execution
   * @throws IOException if the file cannot be read
   */
  public ScriptPlayer(String scriptPath, Agent agent) throws IOException {
    this.scriptPath = scriptPath;
    this.agent = agent;
    this.script = parseScript(scriptPath);
    initializeTaskStatuses();
    initializeCacheFromConfig();
  }

  /**
   * Creates a ScriptPlayer from a YAML file path.
   *
   * @param scriptPath path to the YAML script file
   * @param agent      the agent to use
   * @throws IOException if the file cannot be read
   */
  public ScriptPlayer(Path scriptPath, Agent agent) throws IOException {
    this(scriptPath.toString(), agent);
  }

  /**
   * Parses a YAML script file.
   *
   * @param scriptPath path to the script file
   * @return the parsed script
   * @throws IOException if parsing fails
   */
  public static MidsceneYamlScript parseScript(String scriptPath) throws IOException {
    File file = new File(scriptPath);
    if (!file.exists()) {
      throw new IOException("Script file not found: " + scriptPath);
    }
    return YAML_MAPPER.readValue(file, MidsceneYamlScript.class);
  }

  /**
   * Parses a YAML script from a string.
   *
   * @param yamlContent the YAML content
   * @return the parsed script
   * @throws IOException if parsing fails
   */
  public static MidsceneYamlScript parseScriptFromString(String yamlContent) throws IOException {
    return YAML_MAPPER.readValue(yamlContent, MidsceneYamlScript.class);
  }

  /**
   * Runs the script and returns the result.
   *
   * @return the script execution result
   */
  public ScriptResult run() {
    long startTime = System.currentTimeMillis();
    log.info("Starting script execution: {}",
        scriptPath != null ? scriptPath : "InMemory");

    // Navigate to URL if specified
    if (script.getWeb() != null && script.getWeb().getUrl() != null) {
      String url = script.getWeb().getUrl();
      log.info("Navigating to URL: {}", url);
      try {
        agent.getDriver().navigate(url);
      } catch (Exception e) {
        log.error("Failed to navigate to URL: {}", url, e);
        // If navigation fails, considering implementation, we might want to stop or
        // continue.
        // For now logging error.
      }
    }

    boolean overallSuccess = true;
    for (YamlTask task : script.getTasks()) {
      TaskStatus taskStatus = statusMap.get(task.getName());
      if (taskStatus == null) {
        // Should not happen if initialized correctly
        taskStatus = TaskStatus.init(task.getName(), task.getFlow() != null ? task.getFlow().size() : 0);
        statusMap.put(task.getName(), taskStatus);
      }

      taskStatus.start();
      notifyStatusChange(taskStatus);

      boolean taskSuccess = executeTask(task, taskStatus);

      if (taskSuccess) {
        taskStatus.complete();
      } else {
        // Error already set in executeTask
        if (taskStatus.getStatus() != TaskStatus.Status.ERROR) {
          taskStatus.fail(new RuntimeException("Task failed unknown reason"));
        }
        overallSuccess = false;

        // Check continueOnError from task
        if (task.getContinueOnError() == null || !task.getContinueOnError()) {
          notifyStatusChange(taskStatus);
          break;
        }
      }
      notifyStatusChange(taskStatus);
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    log.info("Script execution finished. Success: {}, Duration: {}ms", overallSuccess, duration);

    ScriptResult.ScriptResultBuilder resultBuilder = ScriptResult.builder()
        .scriptPath(scriptPath)
        .durationMs(duration)
        .executed(true)
        .success(overallSuccess)
        .taskStatuses(new ArrayList<>(statusMap.values()))
        .queryResults(results)
        .resultType(overallSuccess ? ScriptResult.ResultType.SUCCESS : ScriptResult.ResultType.FAILED);

    return resultBuilder.build();
  }

  /**
   * Gets the list of task statuses.
   *
   * @return list of task statuses
   */
  public List<TaskStatus> getTaskStatuses() {
    return new ArrayList<>(statusMap.values());
  }

  /**
   * Executes a single task.
   *
   * @param task   the task to execute
   * @param status the task status tracker
   */
  private boolean executeTask(YamlTask task, TaskStatus status) {
    log.info("Executing task: {}", task.getName());
    try {
      if (task.getFlow() != null) {
        for (int i = 0; i < task.getFlow().size(); i++) {
          status.updateStep(i);
          YamlFlowItem item = task.getFlow().get(i);
          executeFlowItem(item);
        }
      }
      return true;
    } catch (Exception e) {
      log.error("Task failed: {}", task.getName(), e);
      status.fail(e);
      return false;
    }
  }

  /**
   * Executes a single flow item.
   *
   * @param item the flow item to execute
   */
  private void executeFlowItem(YamlFlowItem item) {
    if (item.getAiAction() != null) {
      executeAiAction(item);
    } else if (item.getAiQuery() != null) {
      executeAiQuery(item);
    } else if (item.getAiAssert() != null) {
      executeAiAssert(item);
    } else if (item.getAiWaitFor() != null) {
      executeAiWaitFor(item);
    } else if (item.getSleep() != null) {
      executeSleep(item);
    } else if (item.getJavascript() != null) {
      executeJavaScript(item);
    } else if (item.getLogScreenshot() != null) {
      executeLogScreenshot(item);
    }
  }

  private void executeAiAction(YamlFlowItem item) {
    agent.aiAction(item.getAiAction());
  }

  private void executeAiQuery(YamlFlowItem item) {
    String result = agent.aiQuery(item.getAiQuery());
    if (item.getName() != null) {
      results.put(item.getName(), result);
    }
  }

  private void executeAiAssert(YamlFlowItem item) {
    agent.aiAssert(item.getAiAssert());
  }

  private void executeAiWaitFor(YamlFlowItem item) {
    WaitOptions options = WaitOptions.builder()
        .timeoutMs(item.getTimeout() != null ? item.getTimeout().longValue() : 15000L).build();
    agent.aiWaitFor(item.getAiWaitFor(), options);
  }

  private void executeSleep(YamlFlowItem item) {
    try {
      Thread.sleep(item.getSleep());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void executeJavaScript(YamlFlowItem item) {
    Object result = agent.getDriver().executeScript(item.getJavascript());
    if (item.getName() != null && result != null) {
      results.put(item.getName(), result);
    }
  }

  private void executeLogScreenshot(YamlFlowItem item) {
    String title = item.getLogScreenshot() != null ? item.getLogScreenshot() : "Screenshot";
    log.info("Logging screenshot: {}", title);
    // Logic to actually log or report screenshot could be added here
    // For now taking screenshot to ensure side effect happens if any
    agent.getDriver().takeScreenshot();
  }

  /**
   * Initializes task status objects for all tasks.
   */
  private void initializeTaskStatuses() {
    if (script.getTasks() != null) {
      for (YamlTask task : script.getTasks()) {
        int steps = task.getFlow() != null ? task.getFlow().size() : 0;
        statusMap.put(task.getName(), TaskStatus.init(task.getName(), steps));
      }
    }
  }

  /**
   * Initializes cache from YAML configuration.
   * Note: Cache must ideally be configured when Agent is constructed. This method
   * logs a warning if cache config is found in YAML but agent was provided externally.
   * For full cache support, construct Agent with TaskCache after parsing YAML config.
   */
  private void initializeCacheFromConfig() {
    if (script.getAgent() != null && script.getAgent().getCache() != null) {
      var cacheConfig = script.getAgent().getCache();
      String strategy = cacheConfig.getStrategy();
      
      TaskCache.CacheMode mode;
      if (strategy == null || strategy.isEmpty()) {
        mode = TaskCache.CacheMode.READ_WRITE;
      } else {
        mode = switch (strategy.toLowerCase()) {
          case "read-only" -> TaskCache.CacheMode.READ_ONLY;
          case "write-only" -> TaskCache.CacheMode.WRITE_ONLY;
          case "read-write" -> TaskCache.CacheMode.READ_WRITE;
          case "disabled" -> TaskCache.CacheMode.DISABLED;
          default -> TaskCache.CacheMode.READ_WRITE;
        };
      }
      
      Path cachePath = null;
      if (cacheConfig.getId() != null && !cacheConfig.getId().isEmpty()) {
        cachePath = Path.of(cacheConfig.getId() + ".cache.json");
      }
      
      // Note: This updates agent's cache field but the Orchestrator/Planner already
      // have their own cache reference from construction time. For the cache to work,
      // the agent should be constructed with the cache from the start.
      TaskCache taskCache = TaskCache.withFile(cachePath, mode);
      agent.setCache(taskCache);
      log.warn("Cache configured from YAML (mode={}, id={}), but cache works best when "
          + "Agent is constructed with TaskCache. Consider using Agent.create() with cache parameter.",
          mode, cacheConfig.getId());
    }
  }

  /**
   * Notifies listeners of a task status change.
   *
   * @param status the updated status
   */
  private void notifyStatusChange(TaskStatus status) {
    if (statusListener != null) {
      statusListener.accept(status);
    }
  }

  /**
   * Gets the overall status of the script execution.
   *
   * @return the overall status
   */
  public ScriptResult getOverallStatus() {
    boolean allSuccess = true;
    boolean anyRunning = false;
    for (TaskStatus status : statusMap.values()) {
      if (status.getStatus() == TaskStatus.Status.ERROR) {
        allSuccess = false;
      }
      if (status.getStatus() == TaskStatus.Status.RUNNING || status.getStatus() == TaskStatus.Status.INIT) {
        anyRunning = true;
      }
    }

    return ScriptResult.builder()
        .executed(true)
        .success(!anyRunning && allSuccess)
        .taskStatuses(new ArrayList<>(statusMap.values()))
        .resultType(anyRunning ? ScriptResult.ResultType.NOT_EXECUTED
            : (allSuccess ? ScriptResult.ResultType.SUCCESS : ScriptResult.ResultType.FAILED)) // Simplified
        // logic
        .build();
  }
}
