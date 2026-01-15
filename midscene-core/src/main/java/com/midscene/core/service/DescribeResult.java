package com.midscene.core.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of an element description operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescribeResult {

  /**
   * The description of the element.
   */
  private String description;

  /**
   * Error message if description failed.
   */
  private String error;

  /**
   * Duration of the describe operation in milliseconds.
   */
  private Long durationMs;
}
