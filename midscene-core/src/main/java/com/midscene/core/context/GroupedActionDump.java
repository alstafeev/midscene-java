package com.midscene.core.context;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a grouped action dump containing multiple executions. Mirrors the TypeScript IGroupedActionDump for full
 * report compatibility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupedActionDump {

  /**
   * SDK version string.
   */
  private String sdkVersion;

  /**
   * Name of the test group.
   */
  private String groupName;

  /**
   * Description of the test group.
   */
  private String groupDescription;

  /**
   * List of model names/descriptions used.
   */
  @Builder.Default
  private List<String> modelBriefs = new ArrayList<>();

  /**
   * List of execution dumps.
   */
  @Builder.Default
  private List<ExecutionDump> executions = new ArrayList<>();

  /**
   * Creates a GroupedActionDump from a single Context.
   *
   * @param context    the context to convert
   * @param name       the execution name
   * @param sdkVersion the SDK version
   * @return a GroupedActionDump
   */
  public static GroupedActionDump fromContext(Context context, String name, String sdkVersion) {
    ExecutionDump execution = ExecutionDump.fromContext(context, name);

    return GroupedActionDump.builder()
        .sdkVersion(sdkVersion)
        .groupName(name)
        .executions(List.of(execution))
        .modelBriefs(new ArrayList<>())
        .build();
  }

  /**
   * Adds a model brief to the list.
   *
   * @param modelBrief the model description to add
   */
  public void addModelBrief(String modelBrief) {
    if (modelBrief != null && !modelBriefs.contains(modelBrief)) {
      modelBriefs.add(modelBrief);
    }
  }
}
