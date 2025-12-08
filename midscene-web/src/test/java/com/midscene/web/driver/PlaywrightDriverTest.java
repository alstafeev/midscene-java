package com.midscene.web.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.microsoft.playwright.Keyboard;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.Page;
import com.midscene.core.pojo.planning.Locate;
import com.midscene.core.pojo.type.BySelectorType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlaywrightDriverTest {

  private Page page;
  private PlaywrightDriver playwrightDriver;
  private Mouse mouse;
  private Keyboard keyboard;

  @BeforeEach
  void setUp() {
    page = mock(Page.class);
    mouse = mock(Mouse.class);
    keyboard = mock(Keyboard.class);
    when(page.mouse()).thenReturn(mouse);
    when(page.keyboard()).thenReturn(keyboard);
    playwrightDriver = new PlaywrightDriver(page);

    // Mock page loaded
    when(page.evaluate("document.readyState")).thenReturn("complete");
  }

  @Test
  void testGetUrl() {
    when(page.url()).thenReturn("http://example.com");
    assertEquals("http://example.com", playwrightDriver.getUrl());
  }

  @Test
  void testGetScreenshotBase64() {
    byte[] screenshotBytes = "screenshot".getBytes(StandardCharsets.UTF_8);
    when(page.screenshot(any(Page.ScreenshotOptions.class))).thenReturn(screenshotBytes);
    String expectedBase64 = Base64.getEncoder().encodeToString(screenshotBytes);
    assertEquals(expectedBase64, playwrightDriver.getScreenshotBase64());
  }

  @Test
  void testGetPageSource() {
    when(page.content()).thenReturn("<html></html>");
    assertEquals("<html></html>", playwrightDriver.getPageSource());
  }

  @Test
  void testClickByLocate() {
    Locate locate = new Locate();
    locate.setX(100);
    locate.setY(200);
    playwrightDriver.click(locate);
    verify(mouse).click(100, 200);
  }

  @Test
  void testClickBySelector() {
    Locator locator = mock(Locator.class);
    when(page.locator("xpath=//div")).thenReturn(locator);
    playwrightDriver.click(BySelectorType.BY_XPATH, "//div");
    verify(locator).click();
  }

  @Test
  void testTypeByLocate() {
    Locate locate = new Locate();
    locate.setX(100);
    locate.setY(200);
    playwrightDriver.type(locate, "text");
    verify(mouse).click(100, 200);
    verify(keyboard).type("text");
  }

  @Test
  void testTypeBySelector() {
    Locator locator = mock(Locator.class);
    when(page.locator(".input")).thenReturn(locator);
    playwrightDriver.type(BySelectorType.BY_CSS, ".input", "text");
    verify(locator).fill("text");
  }

  @Test
  void testScrollDownByLocate() {
    Locate locate = new Locate();
    locate.setY(200);
    playwrightDriver.scrollDown(locate);
    verify(mouse).wheel(0, 200);
  }

  @Test
  void testScrollDownBySelector() {
    Locator locator = mock(Locator.class);
    when(page.locator("xpath=//div")).thenReturn(locator);
    playwrightDriver.scrollDown(BySelectorType.BY_XPATH, "//div");
    verify(locator).scrollIntoViewIfNeeded();
  }

  @Test
  void testScrollUpByLocate() {
    Locate locate = new Locate();
    locate.setY(200);
    playwrightDriver.scrollUp(locate);
    verify(mouse).wheel(0, -200);
  }

  @Test
  void testHoverByLocate() {
    Locate locate = new Locate();
    locate.setX(100);
    locate.setY(200);
    playwrightDriver.hover(locate);
    verify(mouse).move(100, 200);
  }

  @Test
  void testHoverBySelector() {
    Locator locator = mock(Locator.class);
    when(page.locator("xpath=//div")).thenReturn(locator);
    playwrightDriver.hover(BySelectorType.BY_XPATH, "//div");
    verify(locator).hover();
  }
}
