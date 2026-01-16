package com.midscene.core.yaml;

import lombok.Data;

/**
 * Represents a flow item in a YAML script task. A flow item can be an action, query, assertion, wait, sleep, or
 * JavaScript execution.
 */
@Data
public class YamlFlowItem {

  // ========== AI Action ==========
  /**
   * AI action to perform (shortcut for aiAct).
   */
  private String ai;

  /**
   * AI action to perform.
   */
  private String aiAct;

  /**
   * AI action to perform (legacy alias).
   */
  private String aiAction;

  /**
   * Whether this action result can be cached.
   */
  private Boolean cacheable;

  /**
   * Planning strategy: "fast" or "default".
   */
  private String planningStrategy;

  // ========== AI Query ==========
  /**
   * AI query to extract data from the page.
   */
  private String aiQuery;

  /**
   * Whether to include DOM in the query context.
   */
  private Boolean domIncluded;

  // ========== AI Assert ==========
  /**
   * AI assertion to verify.
   */
  private String aiAssert;

  /**
   * Error message to display if assertion fails.
   */
  private String errorMessage;

  // ========== AI Wait For ==========
  /**
   * Condition to wait for.
   */
  private String aiWaitFor;

  /**
   * Timeout in milliseconds for wait operations.
   */
  private Long timeout;

  // ========== Sleep ==========
  /**
   * Time to sleep in milliseconds.
   */
  private Long sleep;

  // ========== JavaScript ==========
  /**
   * JavaScript code to execute in the browser.
   */
  private String javascript;

  // ========== Logging ==========
  /**
   * Log a screenshot with optional title.
   */
  private String logScreenshot;

  /**
   * Record content to report.
   */
  private String recordToReport;

  /**
   * Optional content/description.
   */
  private String content;

  // ========== Common ==========
  /**
   * Optional name for the flow item (used for result variables).
   */
  private String name;

  /**
   * Gets the AI action value from any of the action fields.
   *
   * @return the AI action string or null
   */
  public String getAiActionValue() {
    if (ai != null) {
      return ai;
    }
    if (aiAct != null) {
      return aiAct;
    }
    if (aiAction != null) {
      return aiAction;
    }
    return null;
  }

  /**
   * Determines the type of this flow item.
   *
   * @return the flow item type
   */
  public FlowItemType getType() {
    if (getAiActionValue() != null) {
      return FlowItemType.AI_ACTION;
    }
    if (aiQuery != null) {
      return FlowItemType.AI_QUERY;
    }
    if (aiAssert != null) {
      return FlowItemType.AI_ASSERT;
    }
    if (aiWaitFor != null) {
      return FlowItemType.AI_WAIT_FOR;
    }
    if (sleep != null) {
      return FlowItemType.SLEEP;
    }
    if (javascript != null) {
      return FlowItemType.JAVASCRIPT;
    }
    if (logScreenshot != null || recordToReport != null) {
      return FlowItemType.LOG_SCREENSHOT;
    }
    return FlowItemType.UNKNOWN;
  }

  /**
   * Types of flow items.
   */
  public enum FlowItemType {
    AI_ACTION,
    AI_QUERY,
    AI_ASSERT,
    AI_WAIT_FOR,
    SLEEP,
    JAVASCRIPT,
    LOG_SCREENSHOT,
    UNKNOWN
  }
}
