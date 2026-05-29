package com.midscene.web.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

  @Test
  void testClickByCoordinates() {
    com.midscene.core.pojo.planning.Locate locate = new com.midscene.core.pojo.planning.Locate();
    locate.setX(150);
    locate.setY(250);

    seleniumDriver.click(locate);

    org.mockito.ArgumentCaptor<java.util.Collection<org.openqa.selenium.interactions.Sequence>> captor =
        org.mockito.ArgumentCaptor.forClass(java.util.Collection.class);
    verify((Interactive) driver).perform(captor.capture());

    java.util.Collection<org.openqa.selenium.interactions.Sequence> sequences = captor.getValue();
    assertEquals(1, sequences.size());
    org.openqa.selenium.interactions.Sequence sequence = sequences.iterator().next();
    String sequenceStr = sequence.toJson().toString();
    
    // Verify that the pointer movement is absolute (origin is viewport) and has the correct coordinates
    assertTrue(sequenceStr.contains("origin=viewport") || sequenceStr.contains("origin\":\"viewport"));
    assertTrue(sequenceStr.contains("x=150") || sequenceStr.contains("x\":150"));
    assertTrue(sequenceStr.contains("y=250") || sequenceStr.contains("y\":250"));
  }

  @Test
  void testHoverByCoordinates() {
    com.midscene.core.pojo.planning.Locate locate = new com.midscene.core.pojo.planning.Locate();
    locate.setX(300);
    locate.setY(400);

    seleniumDriver.hover(locate);

    org.mockito.ArgumentCaptor<java.util.Collection<org.openqa.selenium.interactions.Sequence>> captor =
        org.mockito.ArgumentCaptor.forClass(java.util.Collection.class);
    verify((Interactive) driver).perform(captor.capture());

    java.util.Collection<org.openqa.selenium.interactions.Sequence> sequences = captor.getValue();
    assertEquals(1, sequences.size());
    org.openqa.selenium.interactions.Sequence sequence = sequences.iterator().next();
    String sequenceStr = sequence.toJson().toString();

    assertTrue(sequenceStr.contains("origin=viewport") || sequenceStr.contains("origin\":\"viewport"));
    assertTrue(sequenceStr.contains("x=300") || sequenceStr.contains("x\":300"));
    assertTrue(sequenceStr.contains("y=400") || sequenceStr.contains("y\":400"));
  }

  @Test
  void testDragAndDropByCoordinates() {
    com.midscene.core.pojo.planning.Locate from = new com.midscene.core.pojo.planning.Locate();
    from.setX(100);
    from.setY(200);

    com.midscene.core.pojo.planning.Locate to = new com.midscene.core.pojo.planning.Locate();
    to.setX(150);
    to.setY(250);

    seleniumDriver.dragAndDrop(from, to);

    org.mockito.ArgumentCaptor<java.util.Collection<org.openqa.selenium.interactions.Sequence>> captor =
        org.mockito.ArgumentCaptor.forClass(java.util.Collection.class);
    verify((Interactive) driver).perform(captor.capture());

    java.util.Collection<org.openqa.selenium.interactions.Sequence> sequences = captor.getValue();
    assertEquals(1, sequences.size());
    org.openqa.selenium.interactions.Sequence sequence = sequences.iterator().next();
    String sequenceStr = sequence.toJson().toString();

    // The sequence should have absolute coordinate moves for both from and to locations
    assertTrue(sequenceStr.contains("x=100") || sequenceStr.contains("x\":100"));
    assertTrue(sequenceStr.contains("y=200") || sequenceStr.contains("y\":200"));
    assertTrue(sequenceStr.contains("x=150") || sequenceStr.contains("x\":150"));
    assertTrue(sequenceStr.contains("y=250") || sequenceStr.contains("y\":250"));
  }
}
