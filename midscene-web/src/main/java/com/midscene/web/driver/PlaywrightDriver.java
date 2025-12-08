package com.midscene.web.driver;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.midscene.core.pojo.planning.Locate;
import com.midscene.core.pojo.type.BySelectorType;
import com.midscene.core.service.PageDriver;
import com.midscene.core.utils.WaitingUtils;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PlaywrightDriver implements PageDriver {

  private final Page page;

  public PlaywrightDriver(Page page) {
    this.page = page;
  }

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
  public void hover(Locate locate) {
    waitUntilPageLoaded();
    page.mouse().move(locate.getX(), locate.getY());
  }

  @Override
  public void hover(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    getLocator(selectorType, elementSelector).hover();
  }

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
