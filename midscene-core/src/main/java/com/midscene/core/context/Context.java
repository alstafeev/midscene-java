package com.midscene.core.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

public class Context {

  @Getter
  private final List<ContextEvent> events = Collections.synchronizedList(new ArrayList<>());

  public void logEvent(ContextEvent event) {
    events.add(event);
  }

  public void logInstruction(String instruction) {
    logEvent(ContextEvent.builder()
        .type("INSTRUCTION")
        .description("User Instruction")
        .data(instruction)
        .timestamp(System.currentTimeMillis())
        .build());
  }

  public void logPlan(String planJson) {
    logEvent(ContextEvent.builder()
        .type("PLAN")
        .description("AI Plan")
        .data(planJson)
        .timestamp(System.currentTimeMillis())
        .build());
  }

  public void logAction(String actionDescription) {
    logEvent(ContextEvent.builder()
        .type("ACTION")
        .description("Executing Action")
        .data(actionDescription)
        .timestamp(System.currentTimeMillis())
        .build());
  }

  public void logError(String errorMessage) {
    logEvent(ContextEvent.builder()
        .type("ERROR")
        .description("Error")
        .data(errorMessage)
        .timestamp(System.currentTimeMillis())
        .build());
  }

  public void logScreenshotBefore(String screenshotBase64) {
    logEvent(ContextEvent.builder()
        .type("SCREENSHOT_BEFORE")
        .description("Screenshot captured")
        .screenshotBase64(screenshotBase64)
        .timestamp(System.currentTimeMillis())
        .build());
  }

  public void logScreenshotAfter(String screenshotBase64) {
    logEvent(ContextEvent.builder()
        .type("SCREENSHOT_AFTER")
        .description("Screenshot captured")
        .screenshotBase64(screenshotBase64)
        .timestamp(System.currentTimeMillis())
        .build());
  }
}
