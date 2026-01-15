package com.midscene.core.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an event in the execution context. Enhanced to support detailed reporting with timing, usage, and AI
 * fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextEvent {

  /**
   * Event type (e.g., "INSTRUCTION", "PLAN", "ACTION", "ERROR", "SCREENSHOT").
   */
  private String type;

  /**
   * Human-readable description.
   */
  private String description;

  /**
   * JSON or text data.
   */
  private String data;

  /**
   * Screenshot as Base64 string.
   */
  private String screenshotBase64;

  /**
   * Event timestamp in milliseconds.
   */
  private long timestamp;

  // ========== Enhanced Fields for Better Reporting ==========

  /**
   * Duration of the event in milliseconds.
   */
  private Long durationMs;

  /**
   * AI's thought/reasoning content.
   */
  private String thought;

  /**
   * Error message if event represents an error.
   */
  private String error;

  /**
   * Output/result of the event.
   */
  private Object output;

  /**
   * Number of tokens used (for AI calls).
   */
  private Integer tokensUsed;

  /**
   * Model name used (for AI calls).
   */
  private String modelName;

  /**
   * Input/prompt tokens (for AI calls).
   */
  private Integer promptTokens;

  /**
   * Output/completion tokens (for AI calls).
   */
  private Integer completionTokens;

  /**
   * Raw AI response.
   */
  private String rawResponse;

  /**
   * Task name for grouping.
   */
  private String taskName;

  /**
   * Sub-type for more specific categorization.
   */
  private String subType;
}
