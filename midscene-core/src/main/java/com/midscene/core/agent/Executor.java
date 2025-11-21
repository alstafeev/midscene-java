package com.midscene.core.agent;

import com.midscene.core.service.PageDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Executor {

  private static final Logger logger = LoggerFactory.getLogger(Executor.class);
  private final PageDriver driver;

  public Executor(PageDriver driver) {
    this.driver = driver;
  }

  public void execute(PlanningAction action) {
    logger.info("Executing action: {}", action.type);
    if ("TAP".equalsIgnoreCase(action.type)) {
      if (action.locate != null) {
        driver.click(action.locate);
      }
    } else if ("TYPE".equalsIgnoreCase(action.type)) {
      if (action.locate != null && action.param != null) {
        driver.type(action.locate, action.param);
      }
    } else if ("SCROLL".equalsIgnoreCase(action.type)) {
      if (action.locate != null) {
        int dx = 0;
        int dy = 500; // Default scroll down
        if (action.param != null && action.param.contains(",")) {
          try {
            String[] parts = action.param.split(",");
            dx = Integer.parseInt(parts[0].trim());
            dy = Integer.parseInt(parts[1].trim());
          } catch (NumberFormatException e) {
            logger.warn("Failed to parse scroll params: {}", action.param);
          }
        }
        driver.scroll(action.locate, dx, dy);
      }
    } else if ("HOVER".equalsIgnoreCase(action.type)) {
      if (action.locate != null) {
        driver.hover(action.locate);
      }
    }
  }
}
