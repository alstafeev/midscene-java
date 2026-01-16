package com.midscene.core.pojo.options;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Options for element location operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocateOptions {

  /**
   * Enable deep think mode for more accurate element location.
   */
  private Boolean deepThink;

  /**
   * Timeout in milliseconds for the locate operation.
   */
  private Long timeout;

  /**
   * Additional prompt to narrow down the search area.
   */
  private String searchAreaPrompt;
}
