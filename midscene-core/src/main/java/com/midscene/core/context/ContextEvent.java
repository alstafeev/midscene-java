package com.midscene.core.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContextEvent {

  private String type; // e.g., "INSTRUCTION", "PLAN", "ACTION", "ERROR", "SCREENSHOT_BEFORE", "SCREENSHOT_AFTER"
  private String description;
  private String data; // JSON or text data
  private String screenshotBase64;
  private long timestamp;
}
