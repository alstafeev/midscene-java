package com.midscene.visualizer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.net.URLEncoder;

public class MidsceneReportGenerator {
  private final String reportTemplate;

  public MidsceneReportGenerator() throws IOException {
    try (var inputStream = getClass().getClassLoader().getResourceAsStream("report_template.html")) {
      if (inputStream == null) {
        throw new IOException("report_template.html not found in classpath");
      }
      this.reportTemplate = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  public MidsceneReportGenerator(Path templatePath) throws IOException {
    this.reportTemplate = Files.readString(templatePath, StandardCharsets.UTF_8);
  }

  public MidsceneReportGenerator(String templateContent) {
    this.reportTemplate = templateContent;
  }

  /**
   * Generates a self-contained HTML report.
   *
   * @param dumpJson   The JSON string containing the execution dump.
   * @param attributes Optional map of attributes (e.g. test_duration, status).
   * @param outputPath Where to save the generated report.
   */
  public void generateReport(String dumpJson, Map<String, String> attributes, Path outputPath) throws IOException {
    String scriptTag = buildScriptTag(dumpJson, attributes);

    // Midscene report template expects the data script to be before the closing
    // </html> tag
    String finalHtml = injectScript(reportTemplate, scriptTag);

    Files.writeString(outputPath, finalHtml, StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  private String buildScriptTag(String dumpJson, Map<String, String> attributes) {
    StringBuilder attrString = new StringBuilder();
    if (attributes != null) {
      for (Map.Entry<String, String> entry : attributes.entrySet()) {
        String key = entry.getKey();
        // Ensure attributes start with "playwright_" if following the pattern, or use
        // as is.
        String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
        attrString.append(String.format(" %s=\"%s\"", key, value));
      }
    }
    // Basic HTML escaping for safety inside the script block if needed,
    // though typically JSON is safe-ish inside script tags unless it contains
    // </script>.
    // A simple replacement for </script> is recommended.
    String safeJson = dumpJson.replace("</script>", "<\\/script>");
    return String.format(
        "<script type=\"midscene_web_dump\"%s>\n%s\n</script>",
        attrString.toString(),
        safeJson);
  }

  private String injectScript(String html, String script) {
    int closeHtmlIndex = html.lastIndexOf("</html>");
    if (closeHtmlIndex == -1) {
      // Fallback: append to end
      return html + "\n" + script;
    }
    return html.substring(0, closeHtmlIndex) + "\n" + script + "\n</html>";
  }
}
