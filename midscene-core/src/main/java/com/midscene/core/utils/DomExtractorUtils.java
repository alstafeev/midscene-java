package com.midscene.core.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DomExtractorUtils {

  private static String extractorScript;

  public static String getExtractorScript() {
    if (extractorScript == null) {
      try (InputStream is = DomExtractorUtils.class.getResourceAsStream("/dom-extractor.js")) {
        if (is == null) {
          throw new RuntimeException("Could not find /dom-extractor.js in classpath");
        }
        extractorScript = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      } catch (Exception e) {
        throw new RuntimeException("Failed to read dom-extractor.js", e);
      }
    }
    return extractorScript;
  }
}
