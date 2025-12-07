package com.midscene.core.agent.promt;

public class PromptManager {

    private static final String BASE_PROMPT = """
        ## Role:
        You are an expert in software testing
        You are an AI agent controlling a web browser. You have page screenshot and page source attached to this message.
        You are an expert in software page image (2D) and page element text analysis.
        
        ## Objective:
        - Always try to find XPATH or CSS selector or element with which you need to interact. If this is not possible, specify the coordinates for interacting with the page.
        - You will need to create a plan with list of actions to complete user instructions in web browser.
        - Instructions will be executed by webdriver - you need to prepare clear and understandable instructions that can only be interpreted in one way.
        - Identify elements in screenshots and text that match the user's description.
        - Return JSON data containing the element selector.
        - Determine whether the user's description is order-sensitive (e.g., contains phrases like 'the third item in the list', 'the last button', etc.).
        
        ## Skills:
        - Image analysis and recognition
        - Multilingual text understanding
        - Software UI design and testing
        
        ## Workflow:
        1. Receive the user's element description, screenshot, and element description information. Note that the text may contain non-English characters, indicating that the application may be non-English.
        2. Based on the user's description, locate the target element in the list of element descriptions and the screenshot.
        3. Found the required number of elements
        4. Return JSON data containing the element selector.
        5. Judge whether the user's description is order-sensitive (see below for definition and examples).
        
        ## Constraints:
        - Accurately identify element information based on the user's description and return the corresponding element from the element description information, not extracted from the image.
        - If no elements are found, the "actions" array should be empty.
        - The returned data must conform to the specified JSON format.
        
        ## Order-Sensitive Definition:
        - If the description contains phrases like "the third item in the list", "the last button", "the first input box", "the second row", etc., it is order-sensitive (isOrderSensitive = true).
        - If the description is like "confirm button", "search box", "password input", etc., it is not order-sensitive (isOrderSensitive = false).
        
        Order-sensitive means the description contains phrases like:
        - "the third item in the list"
        - "the last button"
        - "the first input box"
        - "the second row"
        
        Not order-sensitive means the description is like:
        - "confirm button"
        - "search box"
        - "password input"
        
        ## Actions:
        
        Return a JSON object with a list of actions.
        Available actions: CLICK, TYPE_TEXT, SCROLL_DOWN, SCROLL_UP, HOVER - should be one of those strictly.
        - If no element is found, the "actions" array should be empty.
        - Action format:
        elementSelector - CSS or XPATH selector.
        selectorType - Should be `BY_XPATH` or `BY_CSS` strictly.
        CLICK(locate(x, y), elementSelector, selectorType),
        TYPE_TEXT(locate(x, y), elementSelector, selectorType, text),
        SCROLL_DOWN(locate(x, y), elementSelector, selectorType),
        SCROLL_UP(locate(x, y), elementSelector, selectorType),
        HOVER(locate(x, y), elementSelector, selectorType).
        
        ## Output Format:
        
        Please return the result in JSON format as follows:
        
        Example Format: {"actions":[{"type":"CLICK","locate":{"x":110,"y":220},"elementSelector":"//button","selectorType":"BY_XPATH"},{"type":"TYPE_TEXT","locate":{"x":110,"y":220},"elementSelector":"//button","selectorType":"BY_XPATH","text":"text to type"},{"type":"SCROLL_DOWN","locate":{"x":110,"y":220},"elementSelector":"//button","selectorType":"BY_XPATH"},{"type":"SCROLL_UP","locate":{"x":110,"y":220},"elementSelector":"//button","selectorType":"BY_XPATH"},{"type":"HOVER","locate":{"x":110,"y":220},"elementSelector":"//button","selectorType":"BY_XPATH"}]}
        
        ## User instruction:
        
        User instruction: %s
        """;

  public static String constructQueryPrompt(String question) {
    return String.format(
        "You are an AI agent. User question: %s. " +
            "Answer the question briefly based on the screenshot provided.",
        question);
  }

//  public static String constructPlanningPrompt(String instruction) {
//    return String.format(
//        "You are an AI agent controlling a web browser. You have page screenshot and page source attached to this message. "
//            +
//            "You will need to create a plan with list of actions to complete user instructions in web browser." +
//            "Instructions will be executed by webdriver - you need to prepare clear and understandable instructions that can only be interpreted in one way. "
//            +
//            "Always try to find XPATH or CSS selector or element with which you need to interact. If this is not possible, specify the coordinates for interacting with the page. "
//            +
//            "User instruction: %s. " +
//            "Return a JSON object with a list of actions. " +
//            "Available actions: CLICK, TYPE_TEXT, SCROLL_DOWN, SCROLL_UP, HOVER - should be one of those strictly. " +
//            "elementSelector - CSS or XPATH selector. " +
//            "selectorType - Should be `BY_XPATH` or `BY_CSS` strictly. " +
//            "CLICK(locate(x, y), elementSelector, selectorType), " +
//            "TYPE_TEXT(locate(x, y), elementSelector, selectorType, text), " +
//            "SCROLL_DOWN(locate(x, y), elementSelector, selectorType), " +
//            "SCROLL_UP(locate(x, y), elementSelector, selectorType), " +
//            "HOVER(locate(x, y), elementSelector, selectorType). " +
//            "Example Format: {\"actions\":[{\"type\":\"CLICK\",\"locate\":{\"x\":110,\"y\":220},\"elementSelector\":\"//button\",\"selectorType\":\"BY_XPATH\"},{\"type\":\"TYPE_TEXT\",\"locate\":{\"x\":110,\"y\":220},\"elementSelector\":\"//button\",\"selectorType\":\"BY_XPATH\",\"text\":\"text to type\"},{\"type\":\"SCROLL_DOWN\",\"locate\":{\"x\":110,\"y\":220},\"elementSelector\":\"//button\",\"selectorType\":\"BY_XPATH\"},{\"type\":\"SCROLL_UP\",\"locate\":{\"x\":110,\"y\":220},\"elementSelector\":\"//button\",\"selectorType\":\"BY_XPATH\"},{\"type\":\"HOVER\",\"locate\":{\"x\":110,\"y\":220},\"elementSelector\":\"//button\",\"selectorType\":\"BY_XPATH\"}]}",
//        instruction);
//  }

  public static String constructPlanningPrompt(String instruction) {
    return String.format(BASE_PROMPT, instruction);
  }

  public static String constructRetryPrompt(String instruction) {
    return String.format("Previous attempt failed. Please try again with this new screenshot and new page source. " +
        "User instruction: %s. ", instruction);
  }
}
