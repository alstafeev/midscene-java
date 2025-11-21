package com.midscene.web.demo;

import com.midscene.core.model.MockAIModel;
import com.midscene.web.driver.SeleniumDriver;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class MidsceneSeleniumDemo {

  @Test
  public void testGoogleSearchWithSelenium() {
    // Setup Chrome options
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new"); // Run in headless mode
    options.addArguments("--remote-allow-origins=*");

    WebDriver driver = new ChromeDriver(options);
    try {
      driver.get("https://www.google.com");

      // Initialize Midscene Driver
      SeleniumDriver midsceneDriver = new SeleniumDriver(driver);

      // Initialize AI Model (Mocked for verification)
      MockAIModel aiModel = new MockAIModel();

      // Initialize Agent
      com.midscene.core.agent.Agent agent = new com.midscene.core.agent.Agent(midsceneDriver, aiModel);

      // Run Agent
      agent.aiAction("Search for 'Midscene'");

      // Wait for action
      Thread.sleep(5000);

    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      driver.quit();
    }
  }
}
