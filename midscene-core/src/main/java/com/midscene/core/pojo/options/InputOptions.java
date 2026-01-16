package com.midscene.core.pojo.options;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Options for input operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputOptions {

  /**
   * The value to input.
   */
  private String value;

  /**
   * The input mode: REPLACE (default), APPEND, or CLEAR.
   */
  @Builder.Default
  private InputMode mode = InputMode.REPLACE;

  /**
   * Whether to automatically dismiss the keyboard after input (mobile).
   */
  private Boolean autoDismissKeyboard;

  /**
   * Input mode enum.
   */
  public enum InputMode {
    /**
     * Replace the existing content with new value.
     */
    REPLACE,

    /**
     * Append the value to existing content.
     */
    APPEND,

    /**
     * Clear the input field without entering new value.
     */
    CLEAR
  }
}
