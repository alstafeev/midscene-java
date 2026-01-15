# Midscene Java

**Midscene Java** is an AI-powered automation SDK that allows you to control web browsers using natural language
instructions. It integrates with standard Selenium WebDriver (and Playwright) to serve as an intelligent agent layer on
top of your existing test automation framework.

## Features

* **Natural Language Control**: "Search for 'Headphones' and click the first result."
* **Advanced Interaction**: Click, type, scroll, drag-and-drop, and more using simple commands.
* **Multimodal Understanding**: Uses screenshots to understand page context (Visual Grounding).
* **Smart Planning**: Automatically plans, executes, and retries actions.
* **Service Layer**: Low-level AI capabilities for locating, extracting, and describing elements.
* **YAML Script Support**: Execute declarative test scripts defined in YAML.
* **Caching**: Built-in caching for improved performance and reduced API costs.
* **Framework Agnostic**: Works with Selenium and Playwright.
* **Flexible Configuration**: Supports OpenAI (GPT-4o) and Google Gemini (1.5 Pro) models.
* **Visual Reports**: Generates detailed HTML reports with execution traces, screenshots, and reasoning.

## Modules

* **`midscene-core`**: The brain of the agent. Contains `Agent`, `Service`, `ScriptPlayer`, and core logic.
* **`midscene-web`**: Adapters for browser automation tools (Selenium, Playwright).
* **`midscene-visualizer`**: Generates visual HTML reports from execution contexts.

## Installation

Add the necessary dependencies to your project's `pom.xml`:

```xml
<dependency>
  <groupId>io.github.alstafeev</groupId>
  <artifactId>midscene-web</artifactId>
  <version>0.1.9-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>io.github.alstafeev</groupId>
  <artifactId>midscene-visualizer</artifactId>
  <version>0.1.9-SNAPSHOT</version>
</dependency>
```

## Quick Start (Agent Mode)

Midscene Agent is the primary way to interact with your application. It handles planning and execution.

```java
// 1. Configure
MidsceneConfig config = MidsceneConfig.builder()
    .provider(ModelProvider.GEMINI) // or OPENAI
    .apiKey(System.getenv("GEMINI_API_KEY"))
    .modelName("gemini-1.5-pro")
    .build();

// 2. Initialize (Selenium example)
WebDriver driver = new ChromeDriver();
SeleniumDriver pageDriver = new SeleniumDriver(driver);
Agent agent = Agent.create(config, pageDriver);

// 3. Interact
agent.aiAction("Search for 'Headphones' and click the first result");
agent.aiAssert("Price should be under $200");

// 4. Generate Report
Visualizer.generateReport(agent.getContext(), Paths.get("report.html"));
```

## Advanced Features

### 1. Expanded API Methods

The `Agent` class provides specific methods for precise control:

```java
// Interactions
agent.aiTap("Submit button");
agent.aiInput("Username field", "admin");
agent.aiScroll(ScrollOptions.down());
agent.aiHover("User profile icon");

// Assertions & Waist
agent.aiAssert("The login button should be visible");
agent.aiWaitFor("Welcome message to appear");

// Data Query
String price = agent.aiString("What is the price of the first item?");
boolean isLoggedIn = agent.aiBoolean("Is the user logged in?");
```

### 2. Service Layer (Low-Level AI)

Use the `Service` class for direct AI tasks without full agent planning:

```java
Service service = new Service(pageDriver, agent.getAiModel());

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
ScriptPlayer player = new ScriptPlayer("login_script.yaml", agent);
ScriptResult result = player.run();
```

### 4. Caching

Midscene caches planning results to speed up execution and save tokens.

```java
// Cache is enabled by default (memory + file)
// Configure cache behavior:
MidsceneConfig config = MidsceneConfig.builder()
    // ...
    .cacheId("my_test_cache") // persistent cache file
    .build();
```

## Supported Drivers

- **Selenium**: `new SeleniumDriver(webDriver)`
- **Playwright**: `new PlaywrightDriver(page)`

## Configuration

Detailed configuration options:

```java
MidsceneConfig config = MidsceneConfig.builder()
    .provider(ModelProvider.OPENAI)
    .apiKey("sk-...")
    .modelName("gpt-4o")
    .baseUrl("https://api.openai.com/v1") // optional custom base URL
    .timeoutMs(120000)                    // AI timeout
    .build();
```

## Contributing

Build from source:

```bash
git clone https://github.com/alstafeev/midscene-java.git
cd midscene-java
mvn clean install
```
