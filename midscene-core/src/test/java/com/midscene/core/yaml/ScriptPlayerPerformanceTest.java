package com.midscene.core.yaml;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.midscene.core.agent.Agent;
import com.midscene.core.service.PageDriver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;

class ScriptPlayerPerformanceTest {

  @TempDir
  Path tempDir;

  @Test
  void testRunPerformance() throws IOException {
    // Mock Agent and PageDriver
    Agent agent = mock(Agent.class);
    PageDriver driver = mock(PageDriver.class);
    when(agent.getDriver()).thenReturn(driver);

    // Create a temporary YAML file
    File yamlFile = tempDir.resolve("script.yaml").toFile();
    try (FileWriter writer = new FileWriter(yamlFile)) {
      writer.write("web:\n  url: \"https://example.com\"\ntasks: []");
    }

    // Create ScriptPlayer
    ScriptPlayer player = new ScriptPlayer(yamlFile.getAbsolutePath(), agent);

    // Measure execution time
    long startTime = System.currentTimeMillis();
    player.run();
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // Assert that it takes significantly less than 1000ms after removing Thread.sleep(1000)
    assertTrue(duration < 500, "Execution should be fast (< 500ms) after removing hardcoded sleep. Actual: " + duration + "ms");
  }
}
