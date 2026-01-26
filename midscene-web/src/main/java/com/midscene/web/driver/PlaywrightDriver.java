package com.midscene.web.driver;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.MouseButton;
import com.midscene.core.pojo.planning.Locate;
import com.midscene.core.pojo.type.BySelectorType;
import com.midscene.core.service.PageDriver;
import com.midscene.core.utils.WaitingUtils;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Playwright implementation of the PageDriver interface. Provides browser automation using Microsoft Playwright.
 */
public class PlaywrightDriver implements PageDriver {

  private final Page page;

  public PlaywrightDriver(Page page) {
    this.page = page;
  }

  // ========== Page Information ==========

  @Override
  public String getUrl() {
    waitUntilPageLoaded();
    return page.url();
  }

  @Override
  public String getScreenshotBase64() {
    waitUntilPageLoaded();
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
    return new String(Base64.getEncoder().encode(screenshot), StandardCharsets.UTF_8);
  }

  @Override
  public String getPageSource() {
    waitUntilPageLoaded();
    return page.content();
  }

  // ========== Click/Tap Actions ==========

  @Override
  public void click(Locate locate) {
    waitUntilPageLoaded();
    page.mouse().click(locate.getX(), locate.getY());
  }

  @Override
  public void click(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    getLocator(selectorType, elementSelector).click();
  }

  @Override
  public void doubleClick(Locate locate) {
    waitUntilPageLoaded();
    page.mouse().dblclick(locate.getX(), locate.getY());
  }

  @Override
  public void doubleClick(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    getLocator(selectorType, elementSelector).dblclick();
  }

  @Override
  public void rightClick(Locate locate) {
    waitUntilPageLoaded();
    page.mouse().click(locate.getX(), locate.getY(),
        new com.microsoft.playwright.Mouse.ClickOptions().setButton(MouseButton.RIGHT));
  }

  @Override
  public void rightClick(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    getLocator(selectorType, elementSelector).click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
  }

  @Override
  public void longPress(Locate locate, long durationMs) {
    waitUntilPageLoaded();
    page.mouse().move(locate.getX(), locate.getY());
    page.mouse().down();
    try {
      Thread.sleep(durationMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    page.mouse().up();
  }

  // ========== Text Input Actions ==========

  @Override
  public void type(Locate locate, String text) {
    waitUntilPageLoaded();
    page.mouse().click(locate.getX(), locate.getY());
    page.keyboard().type(text);
  }

  @Override
  public void type(BySelectorType selectorType, String elementSelector, String text) {
    waitUntilPageLoaded();
    getLocator(selectorType, elementSelector).fill(text);
  }

  @Override
  public void clearInput(Locate locate) {
    waitUntilPageLoaded();
    page.mouse().click(locate.getX(), locate.getY());
    // Select all and delete
    page.keyboard().press("Control+a");
    page.keyboard().press("Backspace");
  }

  @Override
  public void clearInput(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    getLocator(selectorType, elementSelector).clear();
  }

  @Override
  public void keyboardPress(String keyName) {
    waitUntilPageLoaded();
    page.keyboard().press(keyName);
  }

  @Override
  public void keyboardPress(Locate locate, String keyName) {
    waitUntilPageLoaded();
    page.mouse().click(locate.getX(), locate.getY());
    page.keyboard().press(keyName);
  }

  // ========== Scroll Actions ==========

  @Override
  public void scrollDown(Locate locate) {
    waitUntilPageLoaded();
    page.mouse().wheel(0, locate.getY());
  }

  @Override
  public void scrollDown(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    getLocator(selectorType, elementSelector).scrollIntoViewIfNeeded();
  }

  @Override
  public void scrollUp(Locate locate) {
    waitUntilPageLoaded();
    page.mouse().wheel(0, -locate.getY());
  }

  @Override
  public void scrollUp(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    getLocator(selectorType, elementSelector).scrollIntoViewIfNeeded();
  }

  @Override
  public void scrollLeft(Locate locate, int distance) {
    waitUntilPageLoaded();
    page.mouse().move(locate.getX(), locate.getY());
    page.mouse().wheel(-distance, 0);
  }

  @Override
  public void scrollRight(Locate locate, int distance) {
    waitUntilPageLoaded();
    page.mouse().move(locate.getX(), locate.getY());
    page.mouse().wheel(distance, 0);
  }

  // ========== Gesture Actions ==========

  @Override
  public void hover(Locate locate) {
    waitUntilPageLoaded();
    page.mouse().move(locate.getX(), locate.getY());
  }

  @Override
  public void hover(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    getLocator(selectorType, elementSelector).hover();
  }

  @Override
  public void swipe(Locate from, Locate to, long durationMs) {
    waitUntilPageLoaded();
    page.mouse().move(from.getX(), from.getY());
    page.mouse().down();
    // Calculate intermediate steps for smooth swipe
    int steps = Math.max(1, (int) (durationMs / 50));
    double dx = (to.getX() - from.getX()) / (double) steps;
    double dy = (to.getY() - from.getY()) / (double) steps;

    for (int i = 1; i <= steps; i++) {
      page.mouse().move(from.getX() + dx * i, from.getY() + dy * i);
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    page.mouse().up();
  }

  @Override
  public void dragAndDrop(Locate from, Locate to) {
    waitUntilPageLoaded();
    page.mouse().move(from.getX(), from.getY());
    page.mouse().down();
    page.mouse().move(to.getX(), to.getY());
    page.mouse().up();
  }

  // ========== Navigation Actions ==========

  @Override
  public void navigate(String url) {
    page.navigate(url);
    waitUntilPageLoaded();
  }

  @Override
  public void reload() {
    page.reload();
    waitUntilPageLoaded();
  }

  @Override
  public void goBack() {
    page.goBack();
    waitUntilPageLoaded();
  }

  // ========== Private Helper Methods ==========

  private void waitUntilPageLoaded() {
    WaitingUtils.waitUntilWithoutException(5, 200, () -> {
      Object readyState = page.evaluate("document.readyState");
      return "complete".equals(readyState);
    }, "Wait until page loaded");
  }

  private Locator getLocator(BySelectorType selectorType, String elementSelector) {
    return switch (selectorType) {
      case BY_XPATH -> page.locator("xpath=" + elementSelector);
      case BY_CSS -> page.locator(elementSelector);
    };
  }
}
