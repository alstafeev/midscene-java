package com.midscene.core.agent;

import com.midscene.core.pojo.planning.ActionsItem;
import com.midscene.core.service.PageDriver;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;

/**
 * Executes actions on the page driver based on the planned action items.
 */
@Log4j2
public class Executor {

  private final PageDriver driver;

  public Executor(PageDriver driver) {
    this.driver = driver;
  }

  /**
   * Executes a single action item.
   *
   * @param action the action to execute
   */
  public void execute(ActionsItem action) {
    log.info("Executing action: {}", action.getType());

    switch (action.getType()) {
      // ========== Click/Tap Actions ==========
      case CLICK, TAP -> executeClick(action);
      case DOUBLE_CLICK -> executeDoubleClick(action);
      case RIGHT_CLICK -> executeRightClick(action);
      case LONG_PRESS -> executeLongPress(action);

      // ========== Text Input Actions ==========
      case TYPE_TEXT, INPUT -> executeTypeText(action);
      case CLEAR_INPUT -> executeClearInput(action);
      case KEYBOARD_PRESS -> executeKeyboardPress(action);

      // ========== Scroll Actions ==========
      case SCROLL_DOWN -> executeScrollDown(action);
      case SCROLL_UP -> executeScrollUp(action);
      case SCROLL -> executeScroll(action);

      // ========== Gesture Actions ==========
      case HOVER -> executeHover(action);
      case SWIPE -> executeSwipe(action);
      case DRAG_AND_DROP -> executeDragAndDrop(action);

      // ========== Navigation Actions ==========
      case NAVIGATE -> driver.navigate(action.getUrl());
      case RELOAD -> driver.reload();
      case GO_BACK -> driver.goBack();

      // ========== Utility Actions ==========
      case SLEEP -> executeSleep(action);
      case ASSERT, WAIT_FOR -> log.info("Assert/WaitFor action: {}", action.getAssertion());
    }
  }

  private void executeClick(ActionsItem action) {
    if (hasSelector(action)) {
      driver.click(action.getSelectorType(), action.getElementSelector());
    } else if (hasLocate(action)) {
      driver.click(action.getLocate());
    }
  }

  private void executeDoubleClick(ActionsItem action) {
    if (hasSelector(action)) {
      driver.doubleClick(action.getSelectorType(), action.getElementSelector());
    } else if (hasLocate(action)) {
      driver.doubleClick(action.getLocate());
    }
  }

  private void executeRightClick(ActionsItem action) {
    if (hasSelector(action)) {
      driver.rightClick(action.getSelectorType(), action.getElementSelector());
    } else if (hasLocate(action)) {
      driver.rightClick(action.getLocate());
    }
  }

  private void executeLongPress(ActionsItem action) {
    if (hasLocate(action)) {
      long duration = Objects.nonNull(action.getDurationMs()) ? action.getDurationMs() : 1000L;
      driver.longPress(action.getLocate(), duration);
    }
  }

  private void executeTypeText(ActionsItem action) {
    String textToType = Objects.nonNull(action.getValue()) ? action.getValue() : action.getText();
    if (hasSelector(action)) {
      driver.type(action.getSelectorType(), action.getElementSelector(), textToType);
    } else if (hasLocate(action) && Objects.nonNull(textToType)) {
      driver.type(action.getLocate(), textToType);
    }
  }

  private void executeClearInput(ActionsItem action) {
    if (hasSelector(action)) {
      driver.clearInput(action.getSelectorType(), action.getElementSelector());
    } else if (hasLocate(action)) {
      driver.clearInput(action.getLocate());
    }
  }

  private void executeKeyboardPress(ActionsItem action) {
    if (Objects.nonNull(action.getKeyName())) {
      if (hasLocate(action)) {
        driver.keyboardPress(action.getLocate(), action.getKeyName());
      } else {
        driver.keyboardPress(action.getKeyName());
      }
    }
  }

  private void executeScrollDown(ActionsItem action) {
    if (hasSelector(action)) {
      driver.scrollDown(action.getSelectorType(), action.getElementSelector());
    } else if (hasLocate(action)) {
      driver.scrollDown(action.getLocate());
    }
  }

  private void executeScrollUp(ActionsItem action) {
    if (hasSelector(action)) {
      driver.scrollUp(action.getSelectorType(), action.getElementSelector());
    } else if (hasLocate(action)) {
      driver.scrollUp(action.getLocate());
    }
  }

  private void executeScroll(ActionsItem action) {
    String direction = action.getDirection();
    if (Objects.isNull(direction)) {
      direction = "down";
    }

    switch (direction.toLowerCase()) {
      case "up" -> executeScrollUp(action);
      case "down" -> executeScrollDown(action);
      case "left" -> {
        int distance = Objects.nonNull(action.getDistance()) ? action.getDistance() : 200;
        if (hasLocate(action)) {
          driver.scrollLeft(action.getLocate(), distance);
        }
      }
      case "right" -> {
        int distance = Objects.nonNull(action.getDistance()) ? action.getDistance() : 200;
        if (hasLocate(action)) {
          driver.scrollRight(action.getLocate(), distance);
        }
      }
    }
  }

  private void executeHover(ActionsItem action) {
    if (hasSelector(action)) {
      driver.hover(action.getSelectorType(), action.getElementSelector());
    } else if (hasLocate(action)) {
      driver.hover(action.getLocate());
    }
  }

  private void executeSwipe(ActionsItem action) {
    if (Objects.nonNull(action.getFrom()) && Objects.nonNull(action.getTo())) {
      long duration = Objects.nonNull(action.getDurationMs()) ? action.getDurationMs() : 500L;
      driver.swipe(action.getFrom(), action.getTo(), duration);
    }
  }

  private void executeDragAndDrop(ActionsItem action) {
    if (Objects.nonNull(action.getFrom()) && Objects.nonNull(action.getTo())) {
      driver.dragAndDrop(action.getFrom(), action.getTo());
    }
  }

  private void executeSleep(ActionsItem action) {
    int sleepMs = Objects.nonNull(action.getSleepMs()) ? action.getSleepMs() : 1000;
    try {
      log.debug("Sleeping for {} ms", sleepMs);
      Thread.sleep(sleepMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Sleep interrupted", e);
    }
  }

  private boolean hasSelector(ActionsItem action) {
    return Objects.nonNull(action.getSelectorType()) && Objects.nonNull(action.getElementSelector());
  }

  private boolean hasLocate(ActionsItem action) {
    return Objects.nonNull(action.getLocate());
  }
}
