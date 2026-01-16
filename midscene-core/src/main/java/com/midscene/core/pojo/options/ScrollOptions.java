package com.midscene.core.pojo.options;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Options for scroll operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrollOptions {

  /**
   * The direction to scroll.
   */
  private ScrollDirection direction;

  /**
   * The type of scroll action to perform.
   */
  private ScrollType scrollType;

  /**
   * The distance to scroll in pixels (for single action scrolls).
   */
  private Integer distance;

  /**
   * Scroll direction enum.
   */
  public enum ScrollDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
  }

  /**
   * Scroll type enum.
   */
  public enum ScrollType {
    /**
     * Perform a single scroll action.
     */
    SINGLE_ACTION,

    /**
     * Scroll until reaching the top.
     */
    SCROLL_TO_TOP,

    /**
     * Scroll until reaching the bottom.
     */
    SCROLL_TO_BOTTOM,

    /**
     * Scroll until reaching the left edge.
     */
    SCROLL_TO_LEFT,

    /**
     * Scroll until reaching the right edge.
     */
    SCROLL_TO_RIGHT
  }
}
