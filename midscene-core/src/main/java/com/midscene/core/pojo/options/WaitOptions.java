package com.midscene.core.pojo.options;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Options for wait operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitOptions {

  /**
   * Maximum time to wait in milliseconds.
   */
  @Builder.Default
  private long timeoutMs = 30000;

  /**
   * Interval between checks in milliseconds.
   */
  @Builder.Default
  private long checkIntervalMs = 1000;

  /**
   * Whether to throw an exception if the wait times out.
   */
  @Builder.Default
  private boolean throwOnTimeout = true;
}
