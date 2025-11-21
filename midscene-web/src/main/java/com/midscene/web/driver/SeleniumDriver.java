package com.midscene.web.driver;

import com.midscene.core.service.PageDriver;
import com.midscene.core.types.Rect;
import com.midscene.core.types.Size;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

public class SeleniumDriver implements PageDriver {

  private final WebDriver driver;

  public SeleniumDriver(WebDriver driver) {
    this.driver = driver;
  }

  @Override
  public String getUrl() {
    return driver.getCurrentUrl();
  }

  @Override
  public Size getViewportSize() {
    org.openqa.selenium.Dimension size = driver.manage().window().getSize();
    return new Size(size.getWidth(), size.getHeight());
  }

  @Override
  public String getScreenshotBase64() {
    return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
  }

  @Override
  public void click(Rect rect) {
    new Actions(driver)
        .moveByOffset((int) (rect.left() + rect.width() / 2), (int) (rect.top() + rect.height() / 2))
        .click()
        .perform();
  }

  @Override
  public void type(Rect rect, String text) {
    new Actions(driver)
        .moveByOffset((int) (rect.left() + rect.width() / 2), (int) (rect.top() + rect.height() / 2))
        .click()
        .sendKeys(text)
        .perform();
  }

  @Override
  public void scroll(Rect rect, int dx, int dy) {
    // Simplified scroll implementation
    new Actions(driver)
        .scrollByAmount(dx, dy)
        .perform();
  }

  @Override
  public void hover(Rect rect) {
    new Actions(driver)
        .moveByOffset((int) (rect.left() + rect.width() / 2), (int) (rect.top() + rect.height() / 2))
        .perform();
  }
}
