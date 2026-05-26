# Midscene Java

**Midscene Java** is an AI-powered automation SDK that allows you to control web browsers using natural language instructions. It integrates with Selenium WebDriver and Playwright to serve as an intelligent agent layer on top of your existing test automation framework.

## Features

* **Natural Language Control**: "Search for 'Headphones' and click the first result."
* **Advanced Interaction**: Click, type, scroll, drag-and-drop, and more using simple natural language commands.
* **Multimodal Understanding**: Uses screenshots to understand page context (Visual Grounding).
* **DOM Grounding**: Automatically extracts a highly compressed JSON snapshot of the visible DOM, drastically reducing token usage and providing 100% accurate element coordinates.
* **Smart Planning**: Automatically plans, executes, and retries actions.
* **Service Layer**: Low-level AI capabilities for locating, extracting, and describing elements.
* **Structured Planning & Assertions**: Relies on structured XML tag responses for robust and predictable planning and assertions.
* **Any AI Model**: Built natively on top of **LangChain4j**, allowing you to use any supported model (OpenAI, Gemini, Claude, local Ollama, etc.) with custom configurations.
* **YAML Script Support**: Execute declarative test scripts defined in YAML.
* **Framework Agnostic**: Works seamlessly with Selenium and Playwright.
* **Visual Reports**: Generates detailed HTML reports with execution traces, screenshots, and reasoning.

## Modules

* **`midscene-core`**: The brain of the agent. Contains `Agent`, `Service`, `ScriptPlayer`, and core logic.
* **`midscene-web`**: Adapters for browser automation tools (Selenium, Playwright).
* **`midscene-visualizer`**: Generates visual HTML reports from execution contexts.

## Installation

Add the necessary dependencies to your project's `pom.xml`. To run the Agent and generate reports, you should add both `midscene-web` and `midscene-visualizer`:

```xml
<dependency>
  <groupId>io.github.alstafeev</groupId>
  <artifactId>midscene-web</artifactId>
  <version>0.1.12</version>
</dependency>

<dependency>
  <groupId>io.github.alstafeev</groupId>
  <artifactId>midscene-visualizer</artifactId>
  <version>0.1.12</version>
</dependency>
```

## Quick Start (Agent Mode)

Midscene Agent is the primary way to interact with your application. Because Midscene Java relies on standard **LangChain4j**, you can inject any `ChatModel` instance directly.

> [!WARNING]
> **Do not configure JSON response formats** (such as `responseFormat("json_object")` or `ResponseFormat.JSON`) on the `ChatModel` builder. Midscene uses XML tags for planning and assertions. Setting the response format to JSON will cause parsing errors.

### Selenium Example

```java
package com.midscene.web;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.midscene.core.agent.Agent;
import com.midscene.web.driver.PlaywrightDriver;
import com.midscene.web.driver.SeleniumDriver;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;

public class LocalTestingTest {

  @Test
  public void localMidsceneSeleniumTest() {
    ChatModel chatModel = GoogleAiGeminiChatModel.builder()
        .apiKey("token")
        .modelName("gemini-3.1-flash-lite")
        .temperature(0.0)
        .build();

    ChromeDriver chromeDriver = new ChromeDriver();
    try {
      chromeDriver.get("https://alstafeev.github.io/");
      SeleniumDriver seleniumDriver = new SeleniumDriver(chromeDriver);

      Agent agent = new Agent(seleniumDriver, chatModel, null, 10);

      agent.aiAction("Search for 'midscene-java' block on the page and click it");
      agent.aiAssert("Github repository 'midscene-java' opened");
    } finally {
      chromeDriver.close();
    }
  }
}

```

### Playwright Example

```java
package com.midscene.web;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.midscene.core.agent.Agent;
import com.midscene.web.driver.PlaywrightDriver;
import com.midscene.web.driver.SeleniumDriver;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;

public class LocalTestingTest {

  @Test
  public void localMidscenePlaywrightTest() {
    ChatModel chatModel = GoogleAiGeminiChatModel.builder()
        .apiKey("token")
        .modelName("gemini-3.1-flash-lite")
        .temperature(0.0)
        .build();

    try (Playwright playwright = Playwright.create();
        Browser chromeBrowser = playwright.chromium().launch()) {
      Page googlePage = chromeBrowser.newPage();
      googlePage.navigate("https://alstafeev.github.io/");

      PlaywrightDriver pageDriver = new PlaywrightDriver(googlePage);
      Agent agent = new Agent(pageDriver, chatModel, null, 10);

      agent.aiAction("Search for 'midscene-java' block on the page and click it");
      agent.aiAssert("Github repository 'midscene-java' opened");
    }
  }
}

```

*(Note: You can still use the `MidsceneConfig` utility class and `Agent.create(config, driver)` for backwards-compatible quick initialization).*

## Advanced Features

### 1. Expanded API Methods

The `Agent` class provides specific methods for precise control:

```java
import com.midscene.core.pojo.options.ScrollOptions;

// Interactions
agent.aiTap("Submit button");
agent.aiInput("Username field", "admin");
agent.aiScroll(ScrollOptions.down());
agent.aiHover("User profile icon");

// Assertions & Waits
agent.aiAssert("The login button should be visible");
agent.aiWaitFor("Welcome message to appear");

// Data Query
String price = agent.aiString("What is the price of the first item?");
boolean isLoggedIn = agent.aiBoolean("Is the user logged in?");
```

### 2. Service Layer (Low-Level AI)

Use the `Service` class for direct AI tasks without full agent planning:

```java
import com.midscene.core.service.Service;
import com.midscene.core.pojo.response.LocateResult;
import com.midscene.core.pojo.response.ExtractResult;
import com.midscene.core.pojo.response.DescribeResult;

Service service = new Service(pageDriver, chatModel);

// Locate element coordinates
LocateResult result = service.locate("The blue checkout button");
System.out.println("Button at: " + result.getRect());

// Extract data
ExtractResult<String> price = service.extract("Price of the main item");

// Describe element
DescribeResult desc = service.describe(100, 200); // describe item at x=100, y=200
```

### 3. YAML Script Support

Define test flows declaratively in YAML:

```yaml
target:
  url: "https://saucedemo.com"

tasks:
  - name: "Login Flow"
    flow:
      - aiAction: "Type 'standard_user' into username field"
      - aiAction: "Type 'secret_sauce' into password field"
      - aiAction: "Click Login"
      - aiAssert: "User should be on the inventory page"
      - logScreenshot: "Inventory Page"
```

Run it with Java:

```java
import com.midscene.core.yaml.ScriptPlayer;
import com.midscene.core.yaml.ScriptResult;

ScriptPlayer player = new ScriptPlayer("login_script.yaml", agent);
ScriptResult result = player.run();
```

### 4. Caching

Midscene caches planning results to speed up execution and save tokens. You can inject a custom `TaskCache` instance when building the Agent.

```java
import com.midscene.core.cache.TaskCache;

Agent agent = new Agent(pageDriver, chatModel, myTaskCache);
```

## Supported Drivers

- **Selenium**: `new SeleniumDriver(webDriver)`
- **Playwright**: `new PlaywrightDriver(page)`

## Contributing

Build from source:

```bash
git clone https://github.com/alstafeev/midscene-java.git
cd midscene-java
mvn clean install
```
