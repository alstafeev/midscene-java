package com.midscene.core.service;

import com.midscene.core.pojo.planning.Locate;
import com.midscene.core.pojo.type.BySelectorType;

/**
 * Interface for browser page interactions. Implementations provide the actual browser automation logic.
 */
public interface PageDriver {

  /**
   * Get the current page URL.
   *
   * @return the current page URL
   */
  String getUrl();

  /**
   * Get screenshot of the current page as Base64 string.
   *
   * @return Base64 string of the screenshot
   */
  String getScreenshotBase64();

  /**
   * Get the current page source code (HTML).
   *
   * @return the current page source code
   */
  String getPageSource();

  /** Click/Tap Actions */

  /**
   * Click on the element located by the instruction.
   *
   * @param locate the locate object which contains the instruction to locate the element
   */
  void click(Locate locate);

  /**
   * Click on the element located by the selector type and element selector.
   *
   * @param selectorType    the selector type, e.g. TEXT, ID, XPATH
   * @param elementSelector the element selector, e.g. "Login"
   */
  void click(BySelectorType selectorType, String elementSelector);

  /**
   * Double-click on the element located by the instruction.
   *
   * @param locate the locate object which contains the instruction to locate the element
   */
  void doubleClick(Locate locate);

  /**
   * Double-click on the element located by the selector type and element selector.
   *
   * @param selectorType    the selector type
   * @param elementSelector the element selector
   */
  void doubleClick(BySelectorType selectorType, String elementSelector);

  /**
   * Right-click on the element located by the instruction.
   *
   * @param locate the locate object which contains the instruction to locate the element
   */
  void rightClick(Locate locate);

  /**
   * Right-click on the element located by the selector type and element selector.
   *
   * @param selectorType    the selector type
   * @param elementSelector the element selector
   */
  void rightClick(BySelectorType selectorType, String elementSelector);

  /**
   * Long press on the element located by the instruction.
   *
   * @param locate     the locate object which contains the instruction to locate the element
   * @param durationMs the duration of the long press in milliseconds
   */
  void longPress(Locate locate, long durationMs);

  /** Text Input Actions */

  /**
   * Type text into the element located by the instruction.
   *
   * @param locate the locate object which contains the instruction to locate the element
   * @param text   the text to type
   */
  void type(Locate locate, String text);

  /**
   * Type text into the element located by the selector type and element selector.
   *
   * @param selectorType    the selector type, e.g. TEXT, ID, XPATH
   * @param elementSelector the element selector, e.g. "Login"
   * @param text            the text to type
   */
  void type(BySelectorType selectorType, String elementSelector, String text);

  /**
   * Clear the input field located by the instruction.
   *
   * @param locate the locate object which contains the instruction to locate the element
   */
  void clearInput(Locate locate);

  /**
   * Clear the input field located by the selector type and element selector.
   *
   * @param selectorType    the selector type
   * @param elementSelector the element selector
   */
  void clearInput(BySelectorType selectorType, String elementSelector);

  /**
   * Press a keyboard key.
   *
   * @param keyName the name of the key to press (e.g., "Enter", "Escape", "Tab")
   */
  void keyboardPress(String keyName);

  /**
   * Press a keyboard key on the element located by the instruction.
   *
   * @param locate  the locate object which contains the instruction to locate the element
   * @param keyName the name of the key to press
   */
  void keyboardPress(Locate locate, String keyName);

  /** Scroll Actions */

  /**
   * Scroll down the element located by the instruction.
   *
   * @param locate the locate object which contains the instruction to locate the element
   */
  void scrollDown(Locate locate);

  /**
   * Scroll down the element located by the selector type and element selector.
   *
   * @param selectorType    the selector type, e.g. TEXT, ID, XPATH
   * @param elementSelector the element selector, e.g. "List"
   */
  void scrollDown(BySelectorType selectorType, String elementSelector);

  /**
   * Scroll up the element located by the instruction.
   *
   * @param locate the locate object which contains the instruction to locate the element
   */
  void scrollUp(Locate locate);

  /**
   * Scroll up the element located by the selector type and element selector.
   *
   * @param selectorType    the selector type, e.g. TEXT, ID, XPATH
   * @param elementSelector the element selector, e.g. "List"
   */
  void scrollUp(BySelectorType selectorType, String elementSelector);

  /**
   * Scroll left from the specified location.
   *
   * @param locate   the starting point for the scroll
   * @param distance the distance to scroll in pixels
   */
  default void scrollLeft(Locate locate, int distance) {
    throw new UnsupportedOperationException("scrollLeft not implemented");
  }

  /**
   * Scroll right from the specified location.
   *
   * @param locate   the starting point for the scroll
   * @param distance the distance to scroll in pixels
   */
  default void scrollRight(Locate locate, int distance) {
    throw new UnsupportedOperationException("scrollRight not implemented");
  }

  // ========== Gesture Actions ==========

  /**
   * Hover over the element located by the instruction.
   *
   * @param locate the locate object which contains the instruction to locate the element
   */
  void hover(Locate locate);

  /**
   * Hover over the element located by the selector type and element selector.
   *
   * @param selectorType    the selector type, e.g. TEXT, ID, XPATH
   * @param elementSelector the element selector, e.g. "Button"
   */
  void hover(BySelectorType selectorType, String elementSelector);

  /**
   * Swipe from one location to another.
   *
   * @param from       the starting location
   * @param to         the ending location
   * @param durationMs the duration of the swipe in milliseconds
   */
  default void swipe(Locate from, Locate to, long durationMs) {
    throw new UnsupportedOperationException("swipe not implemented");
  }

  /**
   * Drag and drop from one element to another.
   *
   * @param from the source location
   * @param to   the target location
   */
  default void dragAndDrop(Locate from, Locate to) {
    throw new UnsupportedOperationException("dragAndDrop not implemented");
  }

  // ========== Navigation Actions ==========

  /**
   * Navigate to a URL.
   *
   * @param url the URL to navigate to
   */
  default void navigate(String url) {
    throw new UnsupportedOperationException("navigate not implemented");
  }

  /**
   * Reload the current page.
   */
  default void reload() {
    throw new UnsupportedOperationException("reload not implemented");
  }

  /**
   * Navigate back in browser history.
   */
  default void goBack() {
    throw new UnsupportedOperationException("goBack not implemented");
  }

  // ========== Utility Actions ==========

  /**
   * Execute JavaScript in the browser.
   *
   * @param script the JavaScript code to execute
   * @return the result of the script execution
   */
  default Object executeScript(String script) {
    throw new UnsupportedOperationException("executeScript not implemented");
  }

  /**
   * Take a screenshot of the current page.
   *
   * @return the screenshot as a byte array
   */
  default byte[] takeScreenshot() {
    throw new UnsupportedOperationException("takeScreenshot not implemented");
  }
}
