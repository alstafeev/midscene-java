package com.midscene.core.agent;

public class PromptManager {

  public static String constructQueryPrompt(String question) {
    return String.format(
        "You are an AI agent. User question: %s. " +
            "Answer the question briefly based on the screenshot provided.",
        question);
  }

  public static String constructPlanningPrompt(String instruction) {
    return String.format(
        "You are an AI agent controlling a web browser. " +
            "User instruction: %s. " +
            "Return a JSON object with a list of actions. " +
            "Available actions: TAP(rect), TYPE(rect, text), SCROLL(rect, dx, dy), HOVER(rect). " +
            "Format: { \"actions\": [ { \"type\": \"TAP\", \"locate\": { \"left\": 0, \"top\": 0, \"width\": 10, \"height\": 10 } } ] }",
        instruction);
  }

  public static String constructRetryPrompt() {
    return "Previous attempt failed. Please try again with this new screenshot.";
  }
}
