package com.midscene.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.midscene.core.model.AIModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceTest {

  private PageDriver driver;
  private AIModel aiModel;
  private Service service;

  @BeforeEach
  void setUp() {
    driver = mock(PageDriver.class);
    aiModel = mock(AIModel.class);
    when(driver.getScreenshotBase64()).thenReturn("base64_image_data");
    service = new Service(driver, aiModel);
  }

  private void mockAiResponse(String content) {
    ChatResponse response = ChatResponse.builder()
        .aiMessage(AiMessage.from(content))
        .build();
    when(aiModel.chat(any())).thenReturn(response);
  }

  @Test
  void testLocateElementFound() {
    String mockResponse = "{\n" +
        "  \"found\": true,\n" +
        "  \"bbox\": [10, 20, 100, 50],\n" +
        "  \"center\": [60, 45],\n" +
        "  \"description\": \"A blue login button\"\n" +
        "}";
    mockAiResponse(mockResponse);

    LocateResult result = service.locate("login button");

    assertNotNull(result.getElement());
    assertEquals(10, result.getRect().getLeft());
    assertEquals(20, result.getRect().getTop());
    assertEquals(100, result.getRect().getWidth());
    assertEquals(50, result.getRect().getHeight());
    assertEquals("A blue login button", result.getElement().getDescription());
    assertEquals(60, result.getElement().getCenter()[0]);
    assertEquals(45, result.getElement().getCenter()[1]);
  }

  @Test
  void testLocateElementNotFound() {
    String mockResponse = "{\n" +
        "  \"found\": false,\n" +
        "  \"reason\": \"Element not visible\"\n" +
        "}";
    mockAiResponse(mockResponse);

    LocateResult result = service.locate("missing button");

    assertTrue(result.getError().contains("Element not found") || result.getElement() == null);
  }

  @Test
  void testExtractData() {
    String mockResponse = "{\n" +
        "  \"data\": \"$100.00\",\n" +
        "  \"thought\": \"Found price tag\"\n" +
        "}";
    mockAiResponse(mockResponse);

    ExtractResult<String> result = service.extract("price");

    assertEquals("$100.00", result.getData());
    assertEquals("Found price tag", result.getThought());
  }

  @Test
  void testExtractStructuredData() {
    String mockResponse = "{\n" +
        "  \"name\": \"John Doe\",\n" +
        "  \"email\": \"john@example.com\",\n" +
        "  \"thought\": \"Extracted user info\"\n" +
        "}";
    mockAiResponse(mockResponse);

    Map<String, String> demand = Map.of("name", "User Name", "email", "User Email");
    ExtractResult<Map<String, Object>> result = service.extract(demand);

    assertNotNull(result.getData());
    assertEquals("John Doe", result.getData().get("name"));
    assertEquals("john@example.com", result.getData().get("email"));
    assertEquals("Extracted user info", result.getThought());
  }

  @Test
  void testDescribeElement() {
    String mockResponse = "{\n" +
        "  \"description\": \"A red submit button with rounded corners\"\n" +
        "}";
    mockAiResponse(mockResponse);

    DescribeResult result = service.describe(100, 200);

    assertEquals("A red submit button with rounded corners", result.getDescription());
  }
}
