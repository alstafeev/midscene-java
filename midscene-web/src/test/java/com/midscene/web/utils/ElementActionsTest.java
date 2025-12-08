package com.midscene.web.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

class ElementActionsTest {

  @Test
  void testIsPageLoaded() {
    WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
    JavascriptExecutor js = (JavascriptExecutor) driver;

    when(js.executeScript("return document.readyState")).thenReturn("complete");
    assertTrue(ElementActions.isPageLoaded.apply(driver));

    when(js.executeScript("return document.readyState")).thenReturn("loading");
    assertFalse(ElementActions.isPageLoaded.apply(driver));
  }

  @Test
  void testIsJQueryCompleted() {
    WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
    JavascriptExecutor js = (JavascriptExecutor) driver;

    when(js.executeScript("return jQuery.active")).thenReturn("0");
    assertTrue(ElementActions.isJQueryCompleted.apply(driver));

    when(js.executeScript("return jQuery.active")).thenReturn("1");
    assertFalse(ElementActions.isJQueryCompleted.apply(driver));
  }
}
