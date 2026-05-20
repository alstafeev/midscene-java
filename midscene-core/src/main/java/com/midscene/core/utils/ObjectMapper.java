package com.midscene.core.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectMapper {

  private final com.fasterxml.jackson.databind.ObjectMapper MAPPER = JsonMapper.builder()
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_VALUES, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .build();

  public <T> T mapResponseToClass(String jsonResponse, Class<T> mappedClass) {
    String clearedJson = cleanMarkdown(jsonResponse);

    try {
      return MAPPER.readValue(clearedJson, mappedClass);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new RuntimeException("Failed to decode json response", e);
    }
  }

  public <T> T mapResponseToClass(String jsonResponse, com.fasterxml.jackson.core.type.TypeReference<T> typeReference) {
    String clearedJson = cleanMarkdown(jsonResponse);

    try {
      return MAPPER.readValue(clearedJson, typeReference);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new RuntimeException("Failed to decode json response", e);
    }
  }

  public String writeValueAsString(Object value) {
    try {
      return MAPPER.writeValueAsString(value);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new RuntimeException("Failed to encode json", e);
    }
  }

  private String cleanMarkdown(String input) {
    if (input == null) return null;
    
    // Find the first occurrence of ``` (with or without language identifier)
    int startIndex = input.indexOf("```");
    if (startIndex != -1) {
      // Find the end of the opening ``` line
      int endOfStartLine = input.indexOf('\n', startIndex);
      if (endOfStartLine != -1) {
        startIndex = endOfStartLine + 1;
      } else {
        startIndex += 3; // Fallback, just skip the backticks
      }
      
      // Find the closing ```
      int endIndex = input.lastIndexOf("```");
      if (endIndex > startIndex) {
        input = input.substring(startIndex, endIndex);
      }
    }
    
    return input.trim();
  }
}
