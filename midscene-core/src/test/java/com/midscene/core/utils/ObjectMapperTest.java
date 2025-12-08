package com.midscene.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import lombok.Data;
import org.junit.jupiter.api.Test;

class ObjectMapperTest {

  @Test
  void testMapSimpleJson() {
    String json = "{\"name\": " + "\"test\", \"value\": 123}";
    TestPojo result = ObjectMapper.mapResponseToClass(json, TestPojo.class);
    assertEquals("test", result.getName());
    assertEquals(123, result.getValue());
  }

  @Test
  void testMapMarkdownJson() {
    String json = "```json\n{\"name\": " + "\"test\", \"value\": 123}\n```";
    TestPojo result = ObjectMapper.mapResponseToClass(json, TestPojo.class);
    assertEquals("test", result.getName());
    assertEquals(123, result.getValue());
  }

  @Test
  void testCaseInsensitive() {
    String json = "{\"NAME\": " + "\"test\", \"VALUE\": 123}";
    TestPojo result = ObjectMapper.mapResponseToClass(json, TestPojo.class);
    assertEquals("test", result.getName());
    assertEquals(123, result.getValue());
  }

  @Data
  static class TestPojo {

    private String name;
    private int value;
  }
}
