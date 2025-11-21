package com.midscene.web.demo;

import com.midscene.core.model.SpyAIModel;
import com.midscene.web.driver.SeleniumDriver;
import dev.langchain4j.data.message.ChatMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class MidsceneComplexActionsTest {

  @Test
  public void testHoverAction() {
    runTest("HOVER", null);
  }

  @Test
  public void testScrollAction() {
    runTest("SCROLL", "0, 200");
  }

  private void runTest(String actionType, String param) {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new");
    options.addArguments("--remote-allow-origins=*");

    WebDriver driver = new ChromeDriver(options);
    try {
      driver.get("https://www.google.com");
      SeleniumDriver midsceneDriver = new SeleniumDriver(driver);
      ActionMockModel mockModel = new ActionMockModel(actionType, param);
      com.midscene.core.agent.Agent agent = new com.midscene.core.agent.Agent(midsceneDriver, mockModel);

      // Action
      agent.aiAction("Test " + actionType);

      // If no exception is thrown, the action was executed (driver methods were called)
      // In a real unit test, we would mock the driver to verify the call.
      // Here we rely on SeleniumDriver not throwing "NotImplemented" or similar.
      System.out.println("Executed " + actionType + " successfully.");

    } finally {
      driver.quit();
    }
  }

  // Custom Spy Model that returns specific actions
  static class ActionMockModel extends SpyAIModel {

    private final String actionType;
    private final String param;

    public ActionMockModel(String actionType, String param) {
      this.actionType = actionType;
      this.param = param;
    }

    @Override
    public String chat(List<ChatMessage> messages) {
      super.chat(messages);
      // Return a plan with the requested action
      // Locate is dummy but valid
      return String.format(
          "{ \"actions\": [ { \"type\": \"%s\", \"param\": \"%s\", \"locate\": { \"left\": 100, \"top\": 100, \"width\": 50, \"height\": 50 } } ] }",
          actionType, param != null ? param : ""
      );
    }
  }
}
