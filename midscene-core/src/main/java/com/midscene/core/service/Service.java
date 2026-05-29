package com.midscene.core.service;

import com.midscene.core.pojo.options.LocateOptions;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;

/**
 * Service class for AI-powered element location, data extraction, and element description. Provides lower-level access
 * to AI capabilities independent of action execution.
 */
@Log4j2
public class Service {

  private final PageDriver driver;
  private final ChatModel chatModel;
  private final Supplier<String> screenshotSupplier;

  /**
   * Creates a new Service with a PageDriver and ChatModel.
   *
   * @param driver    the page driver for screenshots
   * @param chatModel the AI model for processing
   */
  public Service(PageDriver driver, ChatModel chatModel) {
    this.driver = driver;
    this.chatModel = chatModel;
    this.screenshotSupplier = driver::getScreenshotBase64;
  }

  /**
   * Creates a new Service with a custom screenshot supplier.
   *
   * @param screenshotSupplier supplier for screenshot Base64 strings
   * @param chatModel          the AI model for processing
   */
  public Service(Supplier<String> screenshotSupplier, ChatModel chatModel) {
    this.driver = null;
    this.chatModel = chatModel;
    this.screenshotSupplier = screenshotSupplier;
  }

  /**
   * Locates an element on the page based on a natural language description.
   *
   * @param elementDescription description of the element to find
   * @return the locate result with element position
   */
  public LocateResult locate(String elementDescription) {
    return locate(elementDescription, LocateOptions.builder().build());
  }

  /**
   * Locates an element on the page with options.
   *
   * @param elementDescription description of the element to find
   * @param options            locate options (deepThink, timeout, searchArea)
   * @return the locate result with element position
   */
  public LocateResult locate(String elementDescription, LocateOptions options) {
    log.debug("Locating element: {}", elementDescription);
    long startTime = System.currentTimeMillis();

    try {
      String screenshotBase64 = screenshotSupplier.get();

      // Build the prompt for element location
      String prompt = buildLocatePrompt(elementDescription, options);

      // Call AI model
      String response = chatWithImage(prompt, screenshotBase64);

      // Parse the response to extract coordinates
      LocateResult result = parseLocateResponse(response);
      result.setDurationMs(System.currentTimeMillis() - startTime);
      result.setDeepThink(options.getDeepThink());

      log.debug("Element located: {}", result);
      return result;

    } catch (Exception e) {
      log.error("Failed to locate element: {}", elementDescription, e);
      return LocateResult.builder()
          .error(e.getMessage())
          .durationMs(System.currentTimeMillis() - startTime)
          .build();
    }
  }

  /**
   * Extracts data from the page based on a query.
   *
   * @param query the data extraction query
   * @return the extracted data
   */
  public ExtractResult<String> extract(String query) {
    log.debug("Extracting data: {}", query);
    long startTime = System.currentTimeMillis();

    try {
      String screenshotBase64 = screenshotSupplier.get();

      String prompt = buildExtractPrompt(query);
      String response = chatWithImage(prompt, screenshotBase64);

      ExtractResult<String> result = parseExtractResponse(response);
      result.setDurationMs(System.currentTimeMillis() - startTime);

      log.debug("Data extracted: {}", result.getData());
      return result;

    } catch (Exception e) {
      log.error("Failed to extract data: {}", query, e);
      return ExtractResult.<String>builder()
          .error(e.getMessage())
          .durationMs(System.currentTimeMillis() - startTime)
          .build();
    }
  }

  /**
   * Extracts structured data from the page.
   *
   * @param dataDemand map of field names to descriptions
   * @return the extracted data as a map
   */
  public ExtractResult<Map<String, Object>> extract(Map<String, String> dataDemand) {
    log.debug("Extracting structured data: {}", dataDemand);
    long startTime = System.currentTimeMillis();

    try {
      String screenshotBase64 = screenshotSupplier.get();

      String prompt = buildStructuredExtractPrompt(dataDemand);
      String response = chatWithImage(prompt, screenshotBase64);

      ExtractResult<Map<String, Object>> result = parseStructuredExtractResponse(response, dataDemand);
      result.setDurationMs(System.currentTimeMillis() - startTime);

      log.debug("Structured data extracted: {}", result.getData());
      return result;

    } catch (Exception e) {
      log.error("Failed to extract structured data: {}", dataDemand, e);
      return ExtractResult.<Map<String, Object>>builder()
          .error(e.getMessage())
          .durationMs(System.currentTimeMillis() - startTime)
          .build();
    }
  }

  /**
   * Describes an element at the specified coordinates.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   * @return the element description
   */
  public DescribeResult describe(int x, int y) {
    return describe(x, y, false);
  }

  /**
   * Describes an element at the specified coordinates with optional deep think.
   *
   * @param x         the x coordinate
   * @param y         the y coordinate
   * @param deepThink whether to use deep think mode for better accuracy
   * @return the element description
   */
  public DescribeResult describe(int x, int y, boolean deepThink) {
    log.debug("Describing element at ({}, {}), deepThink={}", x, y, deepThink);
    long startTime = System.currentTimeMillis();

    try {
      String screenshotBase64 = screenshotSupplier.get();

      String prompt = buildDescribePrompt(x, y, deepThink);
      String response = chatWithImage(prompt, screenshotBase64);

      DescribeResult result = parseDescribeResponse(response);
      result.setDurationMs(System.currentTimeMillis() - startTime);

      log.debug("Element described: {}", result.getDescription());
      return result;

    } catch (Exception e) {
      log.error("Failed to describe element at ({}, {})", x, y, e);
      return DescribeResult.builder()
          .error(e.getMessage())
          .durationMs(System.currentTimeMillis() - startTime)
          .build();
    }
  }

  /**
   * Describes an element within the specified bounding box.
   *
   * @param rect the bounding rectangle
   * @return the element description
   */
  public DescribeResult describe(LocateResult.Rect rect) {
    int[] center = rect.getCenter();
    return describe(center[0], center[1], false);
  }

  private String chatWithImage(String prompt, String screenshotBase64) {
    UserMessage message;
    if (screenshotBase64 != null) {
      String mimeType = "image/png";
      if (!screenshotBase64.startsWith("data:")) {
        // Assume raw base64 is PNG. If it includes header, we should parse/remove it or
        // specificy correct mime
        // For simplicity assuming raw base64 here if not data URI
      } else {
        // data:image/png;base64,...
        String[] parts = screenshotBase64.split(",");
        screenshotBase64 = parts.length > 1 ? parts[1] : parts[0];
        // mimeType extracted from header if needed, but planner uses fixed image/png
      }

      message = UserMessage.from(
          TextContent.from(prompt),
          ImageContent.from(screenshotBase64, mimeType));
    } else {
      message = UserMessage.from(prompt);
    }

    ChatResponse response = chatModel.chat(Collections.singletonList(message));
    return response.aiMessage().text();
  }

  // ========== Private Helper Methods ==========

  private String buildLocatePrompt(String elementDescription, LocateOptions options) {
    StringBuilder sb = new StringBuilder();
    sb.append("You are analyzing a screenshot to locate a UI element.\n\n");
    sb.append("Find the element described as: ").append(elementDescription).append("\n\n");
    sb.append("Return a JSON response with the following structure:\n");
    sb.append("{\n");
    sb.append("  \"found\": true/false,\n");
    sb.append("  \"bbox\": [left, top, width, height],\n");
    sb.append("  \"center\": [x, y],\n");
    sb.append("  \"description\": \"brief description of what was found\",\n");
    sb.append("  \"reason\": \"explanation if not found\"\n");
    sb.append("}\n");

    if (options.getSearchAreaPrompt() != null) {
      sb.append("\nFocus your search in the area described as: ").append(options.getSearchAreaPrompt());
    }

    return sb.toString();
  }

  private String buildExtractPrompt(String query) {
    return "You are analyzing a screenshot to extract information.\n\n" +
        "Extract: " + query + "\n\n" +
        "Return a JSON response with:\n" +
        "{\n" +
        "  \"data\": \"the extracted value\",\n" +
        "  \"thought\": \"your reasoning process\"\n" +
        "}";
  }

  private String buildStructuredExtractPrompt(Map<String, String> dataDemand) {
    StringBuilder sb = new StringBuilder();
    sb.append("You are analyzing a screenshot to extract structured data.\n\n");
    sb.append("Extract the following fields:\n");
    for (Map.Entry<String, String> entry : dataDemand.entrySet()) {
      sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
    }
    sb.append("\nReturn a JSON response with:\n");
    sb.append("{\n");
    sb.append("  \"data\": { field: value, ... },\n");
    sb.append("  \"thought\": \"your reasoning process\"\n");
    sb.append("}");
    return sb.toString();
  }

  private String buildDescribePrompt(int x, int y, boolean deepThink) {
    StringBuilder sb = new StringBuilder();
    sb.append("You are analyzing a screenshot to describe the UI element at coordinates (")
        .append(x).append(", ").append(y).append(").\n\n");
    if (deepThink) {
      sb.append("Use deep analysis to provide a detailed description.\n\n");
    }
    sb.append("Return a JSON response with:\n");
    sb.append("{\n");
    sb.append("  \"description\": \"detailed description of the element\"\n");
    sb.append("}");
    return sb.toString();
  }

  private LocateResult parseLocateResponse(String response) {
    try {
      com.midscene.core.pojo.response.LocateAiResponse aiResponse =
          com.midscene.core.utils.ObjectMapper.mapResponseToClass(response,
              com.midscene.core.pojo.response.LocateAiResponse.class);

      if (aiResponse == null || !aiResponse.isFound()) {
        return LocateResult.builder()
            .element(null)
            .error(aiResponse != null && aiResponse.getReason() != null ? aiResponse.getReason() : "Element not found")
            .build();
      }

      int[] bbox = aiResponse.getBbox() != null && aiResponse.getBbox().length >= 4 ? aiResponse.getBbox()
          : new int[]{0, 0, 100, 100};
      int[] center = aiResponse.getCenter();

      LocateResult.Rect rect = LocateResult.Rect.builder()
          .left(bbox[0])
          .top(bbox[1])
          .width(bbox[2])
          .height(bbox[3])
          .build();

      LocateResult.LocatedElement element = LocateResult.LocatedElement.builder()
          .center(center != null && center.length >= 2 ? center : rect.getCenter())
          .rect(rect)
          .description(aiResponse.getDescription())
          .build();

      return LocateResult.builder()
          .element(element)
          .rect(rect)
          .build();

    } catch (Exception e) {
      log.warn("Failed to parse locate response: {}", e.getMessage());
      return LocateResult.builder()
          .error("Failed to parse response: " + e.getMessage())
          .build();
    }
  }

  private ExtractResult<String> parseExtractResponse(String response) {
    try {
      com.midscene.core.pojo.response.ExtractAiResponse<String> aiResponse =
          com.midscene.core.utils.ObjectMapper.mapResponseToClass(
              response,
              new com.fasterxml.jackson.core.type.TypeReference<com.midscene.core.pojo.response.ExtractAiResponse<String>>() {
              });

      if (aiResponse == null) {
        throw new RuntimeException("Parsed response is null");
      }

      return ExtractResult.<String>builder()
          .data(aiResponse.getData())
          .thought(aiResponse.getThought())
          .build();
    } catch (Exception e) {
      return ExtractResult.<String>builder()
          .error("Failed to parse response: " + e.getMessage())
          .build();
    }
  }

  private ExtractResult<Map<String, Object>> parseStructuredExtractResponse(String response,
      Map<String, String> dataDemand) {
    try {
      com.midscene.core.pojo.response.ExtractAiResponse<Map<String, Object>> aiResponse =
          com.midscene.core.utils.ObjectMapper.mapResponseToClass(
              response,
              new com.fasterxml.jackson.core.type.TypeReference<com.midscene.core.pojo.response.ExtractAiResponse<Map<String, Object>>>() {
              });

      if (aiResponse == null) {
        throw new RuntimeException("Parsed response is null");
      }

      return ExtractResult.<Map<String, Object>>builder()
          .data(aiResponse.getData())
          .thought(aiResponse.getThought())
          .build();
    } catch (Exception e) {
      return ExtractResult.<Map<String, Object>>builder()
          .error("Failed to parse response: " + e.getMessage())
          .build();
    }
  }

  private DescribeResult parseDescribeResponse(String response) {
    try {
      com.midscene.core.pojo.response.DescribeAiResponse aiResponse =
          com.midscene.core.utils.ObjectMapper.mapResponseToClass(response,
              com.midscene.core.pojo.response.DescribeAiResponse.class);

      if (aiResponse == null) {
        throw new RuntimeException("Parsed response is null");
      }

      return DescribeResult.builder()
          .description(aiResponse.getDescription())
          .build();
    } catch (Exception e) {
      return DescribeResult.builder()
          .error("Failed to parse response: " + e.getMessage())
          .build();
    }
  }
}
