package com.midscene.core.agent.promt;

/**
 * Manages AI prompts for planning, extraction, and assertion operations. Based on the TypeScript version's prompt
 * structure.
 */
public class PromptManager {

  private static final String PLANNING_PROMPT = """
      ## Role
      You are an expert AI agent controlling a web browser. You analyze screenshots and page source to plan and execute actions.
      
      ## Objective
      Plan the NEXT SINGLE ACTION to accomplish the user's instruction. Consider the current page state shown in the screenshot.
      
      ## Skills
      - Image analysis and element recognition
      - Multilingual text understanding
      - Software UI design and testing expertise
      
      ## Available Actions
      %s
      
      ## Workflow
      1. Analyze the current screenshot and page state
      2. Determine if the instruction has been completed
      3. If not complete, plan the single next action
      4. Return structured JSON response
      
      ## Selector Guidelines
      - Prefer XPATH or CSS selectors when elements are identifiable
      - Use coordinates (x, y) as fallback when selectors are not reliable
      - selectorType must be `BY_XPATH` or `BY_CSS`
      
      ## Output Format
      Return a JSON object with the following structure:
      
      ```json
      {
        "log": "Brief description of what you're about to do",
        "moreActionsNeededByInstruction": true|false,
        "actions": [
          {
            "type": "ACTION_TYPE",
            "locate": {"x": 100, "y": 200},
            "elementSelector": "//xpath/or/css",
            "selectorType": "BY_XPATH",
            "text": "optional text for TYPE_TEXT",
            "keyName": "optional key for KEYBOARD_PRESS",
            "direction": "optional for SCROLL",
            "url": "optional for NAVIGATE"
          }
        ],
        "sleep": 0,
        "error": null
      }
      ```
      
      ### Field Descriptions
      - **log**: A brief preamble explaining what you're about to do (use same language as instruction)
      - **moreActionsNeededByInstruction**: true if more actions needed after this one, false if instruction will be complete
      - **actions**: Array of action objects (usually just one). Empty if task is already complete.
      - **sleep**: Optional milliseconds to wait after action (default 0)
      - **error**: Set this if you cannot proceed (explain why)
      
      ## User Instruction
      
      %s
      
      OUTPUT JSON ONLY. NO EXPLANATIONS OR MARKDOWN OUTSIDE THE JSON.
      """;
  private static final String EXTRACTION_PROMPT = """
      ## Role
      You are a professional in software UI design and testing.
      
      ## Task
      Extract data satisfying the DATA_DEMAND from the screenshot.
      
      ## DATA_DEMAND
      %s
      
      ## Output Format
      Return a JSON object:
      ```json
      {
        "thought": "Brief explanation of your analysis",
        "data": <extracted data matching the demand>,
        "errors": []
      }
      ```
      
      ## Rules
      - The data field should match the type expected by the demand
      - If data cannot be found, return null for data and explain in errors
      - Be precise and accurate
      
      OUTPUT JSON ONLY.
      """;
  private static final String ASSERTION_PROMPT = """
      ## Role
      You are evaluating whether a condition is true based on a screenshot.
      
      ## Assertion to Verify
      %s
      
      ## Output Format
      Return a JSON object:
      ```json
      {
        "pass": true|false,
        "thought": "Brief explanation of why the assertion passed or failed"
      }
      ```
      
      Be precise and evaluate ONLY based on what is visible in the screenshot.
      
      OUTPUT JSON ONLY.
      """;

  /**
   * Constructs a planning prompt with dynamic action space description.
   *
   * @param instruction the user instruction to execute
   * @return the formatted planning prompt
   */
  public static String constructPlanningPrompt(String instruction) {
    return String.format(PLANNING_PROMPT, getActionSpaceDescription(), instruction);
  }

  /**
   * Constructs a retry prompt when a previous attempt fails.
   *
   * @param instruction the original user instruction
   * @return the formatted retry prompt
   */
  public static String constructRetryPrompt(String instruction) {
    return String.format("""
        Previous attempt failed. Analyze the new screenshot and page source carefully.
        Consider what may have gone wrong and try an alternative approach.
        
        User instruction: %s
        
        Use the same output format as before.""", instruction);
  }

  /**
   * Constructs a query prompt for answering questions about the page.
   *
   * @param question the question to answer
   * @return the formatted query prompt
   */
  public static String constructQueryPrompt(String question) {
    return String.format("""
        You are an AI assistant analyzing a web page screenshot.
        
        User question: %s
        
        Answer the question briefly and accurately based on the screenshot provided.
        Focus only on what is visible in the current screenshot.""", question);
  }

  /**
   * Constructs an extraction prompt for structured data extraction.
   *
   * @param dataDemand the description of data to extract
   * @return the formatted extraction prompt
   */
  public static String constructExtractionPrompt(String dataDemand) {
    return String.format(EXTRACTION_PROMPT, dataDemand);
  }

  /**
   * Constructs an assertion prompt for verifying conditions.
   *
   * @param assertion the assertion to verify
   * @return the formatted assertion prompt
   */
  public static String constructAssertionPrompt(String assertion) {
    return String.format(ASSERTION_PROMPT, assertion);
  }

  /**
   * Generates a dynamic description of the action space.
   *
   * @return formatted action space description
   */
  private static String getActionSpaceDescription() {
    StringBuilder sb = new StringBuilder();

    sb.append("### Mouse Actions\n");
    sb.append("- **CLICK**: Click on an element. Params: locate{x,y}, elementSelector, selectorType\n");
    sb.append("- **TAP**: Tap on an element (alias for click). Params: locate{x,y}\n");
    sb.append("- **DOUBLE_CLICK**: Double-click on an element. Params: locate{x,y}, elementSelector, selectorType\n");
    sb.append("- **RIGHT_CLICK**: Right-click on an element. Params: locate{x,y}, elementSelector, selectorType\n");
    sb.append("- **HOVER**: Hover over an element. Params: locate{x,y}, elementSelector, selectorType\n");
    sb.append("- **LONG_PRESS**: Long press on an element. Params: locate{x,y}, durationMs\n\n");

    sb.append("### Input Actions\n");
    sb.append("- **TYPE_TEXT**: Type text into an element. Params: locate{x,y}, elementSelector, selectorType, text\n");
    sb.append("- **INPUT**: Input text with mode. Params: locate{x,y}, value, inputMode(replace|append|clear)\n");
    sb.append("- **CLEAR_INPUT**: Clear an input field. Params: locate{x,y}, elementSelector, selectorType\n");
    sb.append("- **KEYBOARD_PRESS**: Press a keyboard key. Params: keyName (e.g., Enter, Tab, Escape)\n\n");

    sb.append("### Scroll Actions\n");
    sb.append("- **SCROLL_DOWN**: Scroll down. Params: locate{x,y}\n");
    sb.append("- **SCROLL_UP**: Scroll up. Params: locate{x,y}\n");
    sb.append("- **SCROLL**: Scroll in a direction. Params: locate{x,y}, direction(up|down|left|right), distance\n\n");

    sb.append("### Gesture Actions\n");
    sb.append("- **SWIPE**: Swipe gesture. Params: from{x,y}, to{x,y}, durationMs\n");
    sb.append("- **DRAG_AND_DROP**: Drag and drop. Params: from{x,y}, to{x,y}\n\n");

    sb.append("### Navigation Actions\n");
    sb.append("- **NAVIGATE**: Navigate to URL. Params: url\n");
    sb.append("- **RELOAD**: Reload the page. No params required.\n");
    sb.append("- **GO_BACK**: Go back in history. No params required.\n\n");

    sb.append("### Utility Actions\n");
    sb.append("- **SLEEP**: Wait for specified time. Params: sleepMs\n");
    sb.append("- **ASSERT**: Assert a condition. Params: assertion\n");
    sb.append("- **WAIT_FOR**: Wait for a condition. Params: assertion, timeoutMs\n");

    return sb.toString();
  }
}
