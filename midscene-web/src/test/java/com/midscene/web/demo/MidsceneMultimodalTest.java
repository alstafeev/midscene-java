package com.midscene.web.demo;

import com.midscene.core.model.SpyAIModel;
import com.midscene.web.driver.SeleniumDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class MidsceneMultimodalTest {

  @Test
  public void testMultimodalPrompting() {
    // Setup Chrome options
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new");
    options.addArguments("--remote-allow-origins=*");

    WebDriver driver = new ChromeDriver(options);
    try {
      driver.get("https://www.google.com");

      // Initialize Midscene Driver
      SeleniumDriver midsceneDriver = new SeleniumDriver(driver);

      // Initialize Spy AI Model
      SpyAIModel spyModel = new SpyAIModel();

      // Initialize Agent
      com.midscene.core.agent.Agent agent = new com.midscene.core.agent.Agent(midsceneDriver, spyModel);

      // Run Agent
      agent.aiAction("Look at this page");

      // Verify Spy Model captured the image
      Assertions.assertNotNull(spyModel.lastMessage, "Should have captured a message");
      Assertions.assertTrue(spyModel.hasTextContent(), "Should have text content");
      Assertions.assertTrue(spyModel.hasImageContent(), "Should have image content");

      System.out.println("Multimodal verification passed: Image and Text sent to AI.");

    } finally {
      driver.quit();
    }
  }
}
