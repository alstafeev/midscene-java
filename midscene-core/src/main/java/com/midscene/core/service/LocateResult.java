package com.midscene.core.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a located element with its position and description.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocateResult {

  /**
   * The located element, or null if not found.
   */
  private LocatedElement element;

  /**
   * The bounding rectangle of the element.
   */
  private Rect rect;

  /**
   * Error message if location failed.
   */
  private String error;

  /**
   * Duration of the locate operation in milliseconds.
   */
  private Long durationMs;

  /**
   * Whether deep think mode was used.
   */
  private Boolean deepThink;

  /**
   * Represents a located element.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LocatedElement {

    /**
     * Center point of the element [x, y].
     */
    private int[] center;

    /**
     * Bounding rectangle of the element.
     */
    private Rect rect;

    /**
     * Description of the element.
     */
    private String description;
  }

  /**
   * Represents a bounding rectangle.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Rect {

    private int left;
    private int top;
    private int width;
    private int height;

    /**
     * Gets the center point of the rectangle.
     *
     * @return the center as [x, y]
     */
    public int[] getCenter() {
      return new int[]{left + width / 2, top + height / 2};
    }
  }
}
