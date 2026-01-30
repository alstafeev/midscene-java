package com.midscene.visualizer;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class MidsceneReportGenerator {

  private final String reportTemplate;

  private static volatile String cachedTemplate = null;
  private static final Object lock = new Object();

  public MidsceneReportGenerator() throws IOException {
    if (cachedTemplate == null) {
      synchronized (lock) {
        if (cachedTemplate == null) {
          cachedTemplate = loadTemplate();
        }
      }
    }
    this.reportTemplate = cachedTemplate;
  }

  private static String loadTemplate() throws IOException {
    String template;
    try (var inputStream = MidsceneReportGenerator.class.getClassLoader().getResourceAsStream("report_template.html")) {
      if (inputStream == null) {
        throw new IOException("report_template.html not found in classpath");
      }
      template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    String favicon;
    try (var inputStream = MidsceneReportGenerator.class.getClassLoader().getResourceAsStream("report_favicon.txt")) {
      if (inputStream == null) {
        throw new IOException("report_favicon.txt not found in classpath");
      }
      favicon = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    return template.replace("__FAVICON__", favicon);
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
    int injectionIndex = html.lastIndexOf("</body>");
    if (injectionIndex == -1) {
      injectionIndex = html.lastIndexOf("</html>");
    }
    if (injectionIndex == -1) {
      // Fallback: append to end
      return html + "\n" + script;
    }
    return html.substring(0, injectionIndex) + "\n" + script + "\n" + html.substring(injectionIndex);
  }
}
