package com.midscene.core.service;

import com.midscene.core.pojo.planning.Locate;
import com.midscene.core.pojo.type.BySelectorType;

public interface PageDriver {

  /**
   * Get the current page URL
   * 
   * @return the current page URL
   */
  String getUrl();

  /**
   * Get screenshot of the current page as Base64 string
   * 
   * @return Base64 string of the screenshot
   */
  String getScreenshotBase64();

  /**
   * Get the current page source code(HTML)
   * 
   * @return the current page source code
   */
  String getPageSource();

  /**
   * Click on the element located by the instruction.
   * 
   * @param locate the locate object which contains the instruction to locate the
   *               element
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
   * Type text into the element located by the instruction.
   * 
   * @param locate the locate object which contains the instruction to locate the
   *               element
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
   * Scroll down the element located by the instruction.
   * 
   * @param locate the locate object which contains the instruction to locate the
   *               element
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
   * @param locate the locate object which contains the instruction to locate the
   *               element
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
   * Hover over the element located by the instruction.
   * 
   * @param locate the locate object which contains the instruction to locate the
   *               element
   */
  void hover(Locate locate);

  /**
   * Hover over the element located by the selector type and element selector.
   * 
   * @param selectorType    the selector type, e.g. TEXT, ID, XPATH
   * @param elementSelector the element selector, e.g. "Button"
   */
  void hover(BySelectorType selectorType, String elementSelector);
}
