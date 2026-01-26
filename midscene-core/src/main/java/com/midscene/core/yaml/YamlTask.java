package com.midscene.core.yaml;

import java.util.List;
import lombok.Data;

/**
 * Represents a task in a YAML script. A task has a name and a list of flow items to execute.
 */
@Data
public class YamlTask {

  /**
   * Name of the task.
   */
  private String name;

  /**
   * List of flow items to execute in this task.
   */
  private List<YamlFlowItem> flow;

  /**
   * If true, continue executing subsequent tasks even if this one fails.
   */
  private Boolean continueOnError;
}
