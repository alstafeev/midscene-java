package com.midscene.core.utils;

import lombok.experimental.UtilityClass;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;

@UtilityClass
public class ObjectMapper {

  private final tools.jackson.databind.ObjectMapper MAPPER = JsonMapper.builder()
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_VALUES, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .build();

  public <T> T mapResponseToClass(String jsonResponse, Class<T> mappedClass) {
    String clearedJson = cleanMarkdown(jsonResponse);

    return MAPPER.readValue(clearedJson, mappedClass);
  }

  public String writeValueAsString(Object value) {
    return MAPPER.writeValueAsString(value);
  }

  private String cleanMarkdown(String input) {
    return input.replaceAll("^```[a-z]*\\s*", "")
        .replaceAll("\\s*```$", "")
        .trim();
  }
}
