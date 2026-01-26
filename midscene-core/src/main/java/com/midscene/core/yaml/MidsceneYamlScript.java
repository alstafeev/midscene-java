package com.midscene.core.yaml;

import java.util.List;
import lombok.Data;

/**
 * Root model class for a Midscene YAML script. Supports web automation with tasks containing flow items.
 */
@Data
public class MidsceneYamlScript {

  /**
   * Web environment configuration.
   */
  private WebEnvironment web;

  /**
   * Agent configuration options.
   */
  private AgentConfig agent;

  /**
   * General configuration options.
   */
  private ScriptConfig config;

  /**
   * List of tasks to execute.
   */
  private List<YamlTask> tasks;

  /**
   * Web environment configuration.
   */
  @Data
  public static class WebEnvironment {

    private String url;
    private String output;
    private String userAgent;
    private Integer viewportWidth;
    private Integer viewportHeight;
    private Double viewportScale;
    private String cookie;
    private Boolean waitForNetworkIdle;
    private Boolean acceptInsecureCerts;
  }

  /**
   * Agent configuration options.
   */
  @Data
  public static class AgentConfig {

    private String testId;
    private String groupName;
    private String groupDescription;
    private Boolean generateReport;
    private String reportFileName;
    private Integer replanningCycleLimit;
    private String aiActionContext;
    private CacheConfig cache;
  }

  /**
   * Cache configuration options.
   */
  @Data
  public static class CacheConfig {

    private String id;
    private String strategy; // "read-only", "write-only", "read-write", "disabled"
  }

  /**
   * Script output configuration.
   */
  @Data
  public static class ScriptConfig {

    private String output;
  }
}
