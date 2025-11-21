package com.midscene.web.demo;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.midscene.core.model.GeminiModel;
import com.midscene.web.driver.PlaywrightDriver;
import org.junit.jupiter.api.Test;

public class MidsceneGeminiDemo {

  @Test
  public void testSauceDemoWithGemini() {
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
      Page page = browser.newPage();
      page.navigate("https://www.saucedemo.com/");

      PlaywrightDriver driver = new PlaywrightDriver(page);
      // Use "gemini-pro" or "gemini-1.5-pro" as model name
      GeminiModel aiModel = new GeminiModel(System.getenv("GEMINI_API_KEY"), "gemini-1.5-pro");

      System.out.println("Navigated to SauceDemo with Gemini");
      System.out.println("Driver initialized: " + driver.getUrl());

      // In a real scenario, we would use the Agent here to interact with the page
      // Agent agent = new Agent(driver, aiModel);
      // agent.act("Login with standard_user and secret_sauce");
    }
  }
}
