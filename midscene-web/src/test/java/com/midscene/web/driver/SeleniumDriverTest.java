package com.midscene.web.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import com.midscene.core.pojo.type.BySelectorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;

class SeleniumDriverTest {

  private WebDriver driver;
  private SeleniumDriver seleniumDriver;
  private JavascriptExecutor js;
  private TakesScreenshot screenshot;

  @BeforeEach
  void setUp() {
    driver = mock(WebDriver.class,
        withSettings().extraInterfaces(JavascriptExecutor.class, TakesScreenshot.class, Interactive.class));
    js = (JavascriptExecutor) driver;
    screenshot = (TakesScreenshot) driver;
    seleniumDriver = new SeleniumDriver(driver);

    // Mock page loaded
    when(js.executeScript("return document.readyState")).thenReturn("complete");
  }

  @Test
  void testGetUrl() {
    when(driver.getCurrentUrl()).thenReturn("http://example.com");
    assertEquals("http://example.com", seleniumDriver.getUrl());
  }

  @Test
  void testGetScreenshotBase64() {
    when(screenshot.getScreenshotAs(OutputType.BASE64)).thenReturn("base64data");
    assertEquals("base64data", seleniumDriver.getScreenshotBase64());
  }

  @Test
  void testGetPageSource() {
    when(driver.getPageSource()).thenReturn("<html></html>");
    assertEquals("<html></html>", seleniumDriver.getPageSource());
  }

  @Test
  void testClickBySelector() {
    WebElement element = mock(WebElement.class);
    when(driver.findElement(By.xpath("//div"))).thenReturn(element);

    seleniumDriver.click(BySelectorType.BY_XPATH, "//div");
    verify(element).click();
  }

  @Test
  void testTypeBySelector() {
    WebElement element = mock(WebElement.class);
    when(driver.findElement(By.cssSelector(".input"))).thenReturn(element);

    seleniumDriver.type(BySelectorType.BY_CSS, ".input", "text");
    verify(element).sendKeys("text");
  }

  @Test
  void testScrollDownBySelector() {
    WebElement element = mock(WebElement.class);
    when(driver.findElement(By.xpath("//div"))).thenReturn(element);

    seleniumDriver.scrollDown(BySelectorType.BY_XPATH, "//div");
    verify(js).executeScript(anyString(), eq(element));
  }
}
