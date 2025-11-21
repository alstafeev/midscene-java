package com.midscene.web.demo;

import com.midscene.core.model.SpyAIModel;
import com.midscene.web.driver.SeleniumDriver;
import dev.langchain4j.data.message.ChatMessage;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class MidsceneReplanningTest {

  @Test
  public void testReplanning() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new");
    options.addArguments("--remote-allow-origins=*");

    WebDriver driver = new ChromeDriver(options);
    try {
      driver.get("https://www.google.com");
      SeleniumDriver midsceneDriver = new SeleniumDriver(driver);
      ReplanningMockModel mockModel = new ReplanningMockModel();
      com.midscene.core.agent.Agent agent = new com.midscene.core.agent.Agent(midsceneDriver, mockModel);

      // Action
      agent.aiAction("Test Replanning");

      // Verify
      Assertions.assertEquals(2, mockModel.attempt, "Should have retried once");
      System.out.println("Replanning verified successfully.");

    } finally {
      driver.quit();
    }
  }

  static class ReplanningMockModel extends SpyAIModel {

    private int attempt = 0;

    @Override
    public String chat(List<ChatMessage> messages) {
      super.chat(messages);
      attempt++;
      if (attempt == 1) {
        // Return invalid JSON to trigger exception
        return "Invalid JSON";
      } else {
        // Return valid plan
        return "{ \"actions\": [ { \"type\": \"TAP\", \"locate\": { \"left\": 100, \"top\": 100, \"width\": 50, \"height\": 50 } } ] }";
      }
    }
  }
}
