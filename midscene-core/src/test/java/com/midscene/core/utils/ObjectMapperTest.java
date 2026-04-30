package com.midscene.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

  @Test
  void testWriteValueAsStringException() {
    CyclicObject cyclicObject = new CyclicObject();
    cyclicObject.setSelf(cyclicObject);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      ObjectMapper.writeValueAsString(cyclicObject);
    });

    assertEquals("Failed to encode json", exception.getMessage());
  }

  @Data
  static class TestPojo {

    private String name;
    private int value;
  }

  @Data
  static class CyclicObject {

    private CyclicObject self;
  }
}
