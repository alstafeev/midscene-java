# Midscene Java

**Midscene Java** is an AI-powered automation SDK that allows you to control web browsers using natural language
instructions. It integrates with standard Selenium WebDriver (and Playwright) to serve as an intelligent agent layer on
top of your existing test automation framework.

## Features

* **Natural Language Control**: "Search for 'Headphones' and click the first result."
* **Multimodal Understanding**: Uses screenshots to understand the page context.
* **Smart Planning**: Automatically plans, executes, and retries actions.
* **Framework Agnostic**: Works alongside your existing Selenium or Playwright tests.
* **Flexible Configuration**: Supports OpenAI (GPT-4o) and Google Gemini models.
* **Visual Reports**: Generates HTML reports with execution traces and screenshots.

## Modules

* **`midscene-core`**: The brain of the agent. Contains the `Planner`, `Executor`, and `Orchestrator`. Pure Java, no
  browser dependencies.
* **`midscene-web`**: Adapters for browser automation tools (Selenium, Playwright).
* **`midscene-visualizer`**: Generates visual HTML reports from execution contexts.

## Installation

### Build Locally

Currently, Midscene Java is available as a source build. Clone this repository and install it to your local Maven
repository:

```bash
git clone https://github.com/alstafeev/midscene-java.git
cd midscene-java
mvn clean install
```

### Add Dependencies

Add the necessary dependencies to your project's `pom.xml`:

```xml

<dependency>
  <groupId>io.github.alstafeev</groupId>
  <artifactId>midscene-web</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
<dependency>
<groupId>io.github.alstafeev</groupId>
<artifactId>midscene-visualizer</artifactId>
<version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Usage Example

Here is how to use Midscene in a standard Selenium test, including report generation.

### Prerequisites

* Java 21 (Recommended) or Java 17+
* Maven
* `OPENAI_API_KEY` or `GEMINI_API_KEY` environment variable set.

### Code Snippet

```java
package io.github.alstafeev.web.demo;

import com.midscene.core.agent.Agent;
import com.midscene.core.config.MidsceneConfig;
import com.midscene.core.config.ModelProvider;
import com.midscene.visualizer.Visualizer;
import com.midscene.web.driver.SeleniumDriver;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Paths;

public class MidsceneDemoTest {

  private WebDriver driver;
  private Agent agent;

  @BeforeEach
  @SneakyThrows
  void initDriver() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--remote-allow-origins=*");
    driver = new ChromeDriver(options);
    driver.manage().window().maximize();
  }

  @Test
  public void localGeminiTest() {
    driver.get("https://midscenejs.com/");

    MidsceneConfig config = MidsceneConfig.builder()
        .provider(ModelProvider.GEMINI)
        .apiKey(System.getenv("GEMINI_API_KEY"))
        .modelName("gemini-2.5-pro")
        .build();

    SeleniumDriver driverAdapter = new SeleniumDriver(driver);
    agent = Agent.create(config, driverAdapter);

    agent.aiAction("Search for 'MCP server' button in the left sidebar of this site and click it.");
  }

  @AfterEach
  void shutDownDriver() {
    if (agent != null) {
      // Generate report after test
      Visualizer.generateReport(agent.getContext(), Paths.get("midscene-report.html"));
    }
    if (driver != null) {
      driver.quit();
    }
  }
}
```

### Playwright Example

```java
package io.github.alstafeev.web.demo;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.midscene.core.agent.Agent;
import com.midscene.core.config.MidsceneConfig;
import com.midscene.core.config.ModelProvider;
import com.midscene.visualizer.Visualizer;
import com.midscene.web.driver.PlaywrightDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MidscenePlaywrightDemoTest {

  private Playwright playwright;
  private Browser browser;
  private Page page;
  private Agent agent;

  @BeforeEach
  void initDriver() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    page = browser.newPage();
  }

  @Test
  public void localGeminiTest() {
    page.navigate("https://midscenejs.com/");

    MidsceneConfig config = MidsceneConfig.builder()
        .provider(ModelProvider.GEMINI)
        .apiKey(System.getenv("GEMINI_API_KEY"))
        .modelName("gemini-2.5-pro")
        .build();

    PlaywrightDriver driverAdapter = new PlaywrightDriver(page);
    agent = Agent.create(config, driverAdapter);

    agent.aiAction("Search for 'MCP server' button in the left sidebar of this site and click it.");
  }

  @AfterEach
  void shutDownDriver() {
    if (agent != null) {
      // Generate report after test
      Visualizer.generateReport(agent.getContext(), Paths.get("midscene-playwright-report.html"));
    }
    if (browser != null) {
      browser.close();
    }
    if (playwright != null) {
      playwright.close();
    }
  }
}
```

## Configuration

You can configure the agent using `MidsceneConfig`:

```java
MidsceneConfig config = MidsceneConfig.builder()
    .provider(ModelProvider.GEMINI)      // Choose OPENAI or GEMINI
    .apiKey("your-api-key")              // Set API Key
    .modelName("gemini-2.5-pro")         // Specific model version
    .timeoutMs(60000)                    // Timeout in milliseconds
    .build();
```
