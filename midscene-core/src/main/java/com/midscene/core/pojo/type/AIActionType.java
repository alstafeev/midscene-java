package com.midscene.core.pojo.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of all AI action types that can be executed. Matches the action space defined in the TypeScript version.
 */
@Getter
@AllArgsConstructor
public enum AIActionType {
  // Basic mouse actions
  CLICK("click"),
  TAP("tap"),
  DOUBLE_CLICK("double_click"),
  RIGHT_CLICK("right_click"),
  HOVER("hover"),
  LONG_PRESS("long_press"),

  // Text input actions
  TYPE_TEXT("type_text"),
  INPUT("input"),
  CLEAR_INPUT("clear_input"),
  KEYBOARD_PRESS("keyboard_press"),

  // Scroll actions
  SCROLL("scroll"),
  SCROLL_DOWN("scroll_down"),
  SCROLL_UP("scroll_up"),

  // Gesture actions
  SWIPE("swipe"),
  DRAG_AND_DROP("drag_and_drop"),

  // Navigation actions
  NAVIGATE("navigate"),
  RELOAD("reload"),
  GO_BACK("go_back"),

  // Utility actions
  SLEEP("sleep"),
  ASSERT("assert"),
  WAIT_FOR("wait_for");

  private final String value;
}
