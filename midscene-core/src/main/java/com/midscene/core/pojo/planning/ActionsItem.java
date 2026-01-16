package com.midscene.core.pojo.planning;

import com.midscene.core.pojo.type.AIActionType;
import com.midscene.core.pojo.type.BySelectorType;
import lombok.Data;

/**
 * Represents a single action item returned by the AI planner.
 */
@Data
public class ActionsItem {

  // Element location
  private Locate locate;
  private String elementSelector;
  private BySelectorType selectorType;

  // Action type
  private AIActionType type;

  // Text input
  private String text;
  private String value;
  private String inputMode; // replace, append, clear

  // Keyboard
  private String keyName;

  // Scroll
  private String direction; // up, down, left, right
  private String scrollType; // singleAction, scrollToTop, scrollToBottom
  private Integer distance;

  // Navigation
  private String url;

  // Gesture
  private Locate from;
  private Locate to;
  private Long durationMs;

  // Sleep
  private Integer sleepMs;

  // Assert/WaitFor
  private String assertion;
  private Long timeoutMs;
}