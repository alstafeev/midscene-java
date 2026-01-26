package com.midscene.core.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of a data extraction operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractResult<T> {

  /**
   * The extracted data.
   */
  private T data;

  /**
   * AI's reasoning/thought process.
   */
  private String thought;

  /**
   * Error message if extraction failed.
   */
  private String error;

  /**
   * Duration of the extraction in milliseconds.
   */
  private Long durationMs;

  /**
   * Number of tokens used.
   */
  private Integer tokensUsed;
}
