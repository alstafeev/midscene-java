package com.midscene.web.demo;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.midscene.core.model.OpenAIModel;
import com.midscene.web.driver.PlaywrightDriver;
import org.junit.jupiter.api.Test;

public class MidsceneDemo {

  @Test
  public void testSauceDemo() {
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      page.navigate("https://www.saucedemo.com/");

      PlaywrightDriver driver = new PlaywrightDriver(page);
      // 2. Create AI Model
      OpenAIModel aiModel = new OpenAIModel(System.getenv("OPENAI_API_KEY"), "gpt-4o");

      // 3. Create Agent
      com.midscene.core.agent.Agent agent = new com.midscene.core.agent.Agent(driver, aiModel);

      // 4. Run Agent
      agent.aiAction("Search for 'Midscene' on Google");

      // Wait a bit to see result
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      playwright.close();
    }
  }
}
