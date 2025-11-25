# Midscene Java

**Midscene Java** is an AI-powered automation SDK that allows you to control web browsers using natural language instructions. It integrates with standard Selenium WebDriver (and Playwright) to serve as an intelligent agent layer on top of your existing test automation framework.

## Features

*   **Natural Language Control**: "Search for 'Headphones' and click the first result."
*   **Multimodal Understanding**: Uses screenshots to understand the page context.
*   **Smart Planning**: Automatically plans, executes, and retries actions.
*   **Framework Agnostic**: Works alongside your existing Selenium or Playwright tests.
*   **Flexible Configuration**: Supports OpenAI (GPT-4o) and Google Gemini models.

## Installation

### 1. Build Locally
Currently, Midscene Java is available as a source build. Clone this repository and install it to your local Maven repository:

```bash
git clone https://github.com/midscene/midscene-java.git
cd midscene-java
mvn clean install
```

### 2. Add Dependency
Add the `midscene-web` dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.midscene</groupId>
    <artifactId>midscene-web</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Usage Example

Here is how to use Midscene in a standard Selenium test.

### Prerequisites
*   Java 17+
*   Maven
*   `OPENAI_API_KEY` or `GEMINI_API_KEY` environment variable set.

### Code Snippet

```java
package com.midscene.web.demo;

import com.midscene.core.agent.Agent;
import com.midscene.core.config.MidsceneConfig;
import com.midscene.core.config.ModelProvider;
import com.midscene.web.driver.SeleniumDriver;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@Log4j2
public class MidsceneDemoTest {

  private WebDriver driver;

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
        .apiKey("API_KEY")
        .modelName("gemini-2.5-pro")
        .build();

    SeleniumDriver driverAdapter = new SeleniumDriver(driver);
    Agent agent = Agent.create(config, driverAdapter);

    agent.aiAction("Search for 'MCP server' button in the left sidebar of this site and click it.");
  }

  @AfterEach
  void shutDownDriver() {
    driver.quit();
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

## Architecture

*   **`midscene-core`**: The brain of the agent. Contains the `Planner`, `Executor`, and `Orchestrator`. It is pure Java and has no dependency on Selenium/Playwright.
*   **`midscene-web`**: Adapters for browser automation tools. Currently supports `SeleniumDriver` and `PlaywrightDriver`.

## Requirements

*   JDK 21 (Recommended) or JDK 17+
*   Maven 3.8+
