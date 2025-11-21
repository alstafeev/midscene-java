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

public class MidsceneInsightTest {

  @Test
  public void testInsightQuery() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new");
    options.addArguments("--remote-allow-origins=*");

    WebDriver driver = new ChromeDriver(options);
    try {
      driver.get("https://www.google.com");
      SeleniumDriver midsceneDriver = new SeleniumDriver(driver);
      InsightMockModel mockModel = new InsightMockModel();
      com.midscene.core.agent.Agent agent = new com.midscene.core.agent.Agent(midsceneDriver, mockModel);

      // Query
      String answer = agent.aiQuery("What is this page?");

      // Verify
      Assertions.assertEquals("This is a Google search page.", answer);
      Assertions.assertTrue(mockModel.hasImageContent(), "Should have sent an image");
      Assertions.assertTrue(mockModel.hasTextContent(), "Should have sent text");

      System.out.println("Insight query verified successfully.");

    } finally {
      driver.quit();
    }
  }

  static class InsightMockModel extends SpyAIModel {

    @Override
    public String chat(List<ChatMessage> messages) {
      super.chat(messages);
      // Return a dummy answer
      return "This is a Google search page.";
    }
  }
}
