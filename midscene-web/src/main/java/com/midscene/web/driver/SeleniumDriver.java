package com.midscene.web.driver;

import com.midscene.core.pojo.planning.Locate;
import com.midscene.core.pojo.type.BySelectorType;
import com.midscene.core.service.PageDriver;
import com.midscene.core.utils.WaitingUtils;
import com.midscene.web.utils.ElementActions;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * Selenium implementation of the PageDriver interface. Provides browser automation using Selenium WebDriver.
 */
public class SeleniumDriver implements PageDriver {

  private final WebDriver driver;

  public SeleniumDriver(WebDriver driver) {
    this.driver = driver;
  }

  // ========== Page Information ==========

  @Override
  public String getUrl() {
    waitUntilPageLoaded();
    return driver.getCurrentUrl();
  }

  @Override
  public String getScreenshotBase64() {
    waitUntilPageLoaded();
    return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
  }

  @Override
  public String getPageSource() {
    waitUntilPageLoaded();
    return driver.getPageSource();
  }

  // ========== Click/Tap Actions ==========

  @Override
  public void click(Locate locate) {
    waitUntilPageLoaded();
    new Actions(driver)
        .moveByOffset(locate.getX(), locate.getY())
        .click()
        .perform();
  }

  @Override
  public void click(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    getWebElementBySelector(selectorType, elementSelector).click();
  }

  @Override
  public void doubleClick(Locate locate) {
    waitUntilPageLoaded();
    new Actions(driver)
        .moveByOffset(locate.getX(), locate.getY())
        .doubleClick()
        .perform();
  }

  @Override
  public void doubleClick(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    WebElement element = getWebElementBySelector(selectorType, elementSelector);
    new Actions(driver).doubleClick(element).perform();
  }

  @Override
  public void rightClick(Locate locate) {
    waitUntilPageLoaded();
    new Actions(driver)
        .moveByOffset(locate.getX(), locate.getY())
        .contextClick()
        .perform();
  }

  @Override
  public void rightClick(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    WebElement element = getWebElementBySelector(selectorType, elementSelector);
    new Actions(driver).contextClick(element).perform();
  }

  @Override
  public void longPress(Locate locate, long durationMs) {
    waitUntilPageLoaded();
    new Actions(driver)
        .moveByOffset(locate.getX(), locate.getY())
        .clickAndHold()
        .pause(Duration.ofMillis(durationMs))
        .release()
        .perform();
  }

  // ========== Text Input Actions ==========

  @Override
  public void type(Locate locate, String text) {
    waitUntilPageLoaded();
    new Actions(driver)
        .moveByOffset(locate.getX(), locate.getY())
        .click()
        .sendKeys(text)
        .perform();
  }

  @Override
  public void type(BySelectorType selectorType, String elementSelector, String text) {
    waitUntilPageLoaded();
    getWebElementBySelector(selectorType, elementSelector).sendKeys(text);
  }

  @Override
  public void clearInput(Locate locate) {
    waitUntilPageLoaded();
    new Actions(driver)
        .moveByOffset(locate.getX(), locate.getY())
        .click()
        .keyDown(Keys.CONTROL)
        .sendKeys("a")
        .keyUp(Keys.CONTROL)
        .sendKeys(Keys.BACK_SPACE)
        .perform();
  }

  @Override
  public void clearInput(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    WebElement element = getWebElementBySelector(selectorType, elementSelector);
    element.clear();
  }

  @Override
  public void keyboardPress(String keyName) {
    waitUntilPageLoaded();
    Keys key = mapKeyNameToSeleniumKey(keyName);
    new Actions(driver).sendKeys(key).perform();
  }

  @Override
  public void keyboardPress(Locate locate, String keyName) {
    waitUntilPageLoaded();
    Keys key = mapKeyNameToSeleniumKey(keyName);
    new Actions(driver)
        .moveByOffset(locate.getX(), locate.getY())
        .click()
        .sendKeys(key)
        .perform();
  }

  // ========== Scroll Actions ==========

  @Override
  public void scrollDown(Locate locate) {
    waitUntilPageLoaded();
    new Actions(driver)
        .scrollByAmount(0, locate.getY())
        .perform();
  }

  @Override
  public void scrollDown(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
        getWebElementBySelector(selectorType, elementSelector));
  }

  @Override
  public void scrollUp(Locate locate) {
    waitUntilPageLoaded();
    new Actions(driver)
        .scrollByAmount(0, -locate.getY())
        .perform();
  }

  @Override
  public void scrollUp(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
        getWebElementBySelector(selectorType, elementSelector));
  }

  @Override
  public void scrollLeft(Locate locate, int distance) {
    waitUntilPageLoaded();
    new Actions(driver)
        .scrollByAmount(-distance, 0)
        .perform();
  }

  @Override
  public void scrollRight(Locate locate, int distance) {
    waitUntilPageLoaded();
    new Actions(driver)
        .scrollByAmount(distance, 0)
        .perform();
  }

  // ========== Gesture Actions ==========

  @Override
  public void hover(Locate locate) {
    waitUntilPageLoaded();
    new Actions(driver)
        .moveByOffset(locate.getX(), locate.getY())
        .perform();
  }

  @Override
  public void hover(BySelectorType selectorType, String elementSelector) {
    waitUntilPageLoaded();
    WebElement element = getWebElementBySelector(selectorType, elementSelector);
    new Actions(driver).moveToElement(element).perform();
  }

  @Override
  public void swipe(Locate from, Locate to, long durationMs) {
    waitUntilPageLoaded();
    int steps = Math.max(1, (int) (durationMs / 50));
    int dx = (to.getX() - from.getX()) / steps;
    int dy = (to.getY() - from.getY()) / steps;

    Actions actions = new Actions(driver)
        .moveByOffset(from.getX(), from.getY())
        .clickAndHold();

    for (int i = 0; i < steps; i++) {
      actions = actions.moveByOffset(dx, dy).pause(Duration.ofMillis(50));
    }

    actions.release().perform();
  }

  @Override
  public void dragAndDrop(Locate from, Locate to) {
    waitUntilPageLoaded();
    new Actions(driver)
        .moveByOffset(from.getX(), from.getY())
        .clickAndHold()
        .moveByOffset(to.getX() - from.getX(), to.getY() - from.getY())
        .release()
        .perform();
  }

  // ========== Navigation Actions ==========

  @Override
  public void navigate(String url) {
    driver.get(url);
    waitUntilPageLoaded();
  }

  @Override
  public void reload() {
    driver.navigate().refresh();
    waitUntilPageLoaded();
  }

  @Override
  public void goBack() {
    driver.navigate().back();
    waitUntilPageLoaded();
  }

  // ========== Private Helper Methods ==========

  public void waitUntilPageLoaded() {
    WaitingUtils.waitUntilWithoutException(2, 2000, () -> ElementActions.isPageLoaded.apply(driver),
        "Wait until page loaded");
  }

  public WebElement getWebElementBySelector(BySelectorType selectorType, String elementSelector) {
    return switch (selectorType) {
      case BY_XPATH -> driver.findElement(By.xpath(elementSelector));
      case BY_CSS -> driver.findElement(By.cssSelector(elementSelector));
    };
  }

  /**
   * Maps a key name string to Selenium Keys enum.
   */
  private Keys mapKeyNameToSeleniumKey(String keyName) {
    return switch (keyName.toLowerCase()) {
      case "enter", "return" -> Keys.ENTER;
      case "tab" -> Keys.TAB;
      case "escape", "esc" -> Keys.ESCAPE;
      case "backspace" -> Keys.BACK_SPACE;
      case "delete" -> Keys.DELETE;
      case "space" -> Keys.SPACE;
      case "arrowup", "up" -> Keys.ARROW_UP;
      case "arrowdown", "down" -> Keys.ARROW_DOWN;
      case "arrowleft", "left" -> Keys.ARROW_LEFT;
      case "arrowright", "right" -> Keys.ARROW_RIGHT;
      case "home" -> Keys.HOME;
      case "end" -> Keys.END;
      case "pageup" -> Keys.PAGE_UP;
      case "pagedown" -> Keys.PAGE_DOWN;
      case "f1" -> Keys.F1;
      case "f2" -> Keys.F2;
      case "f3" -> Keys.F3;
      case "f4" -> Keys.F4;
      case "f5" -> Keys.F5;
      case "f6" -> Keys.F6;
      case "f7" -> Keys.F7;
      case "f8" -> Keys.F8;
      case "f9" -> Keys.F9;
      case "f10" -> Keys.F10;
      case "f11" -> Keys.F11;
      case "f12" -> Keys.F12;
      case "control", "ctrl" -> Keys.CONTROL;
      case "alt" -> Keys.ALT;
      case "shift" -> Keys.SHIFT;
      case "meta", "command", "cmd" -> Keys.META;
      default -> Keys.valueOf(keyName.toUpperCase());
    };
  }
}
