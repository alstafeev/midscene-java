package com.midscene.core.yaml;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;

import com.midscene.core.agent.Agent;
import com.midscene.core.service.PageDriver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScriptPlayerSecurityTest {

  @TempDir
  Path tempDir;

  @Test
  void testJavaScriptDisabledByDefault() throws IOException {
    Agent agent = mock(Agent.class);
    when(agent.isJavaScriptExecutionEnabled()).thenReturn(false);

    File yamlFile = tempDir.resolve("script_disabled.yaml").toFile();
    try (FileWriter writer = new FileWriter(yamlFile)) {
      writer.write("tasks:\n  - name: test\n    flow:\n      - javascript: \"console.log('hello')\"");
    }

    ScriptPlayer player = new ScriptPlayer(yamlFile.getAbsolutePath(), agent);
    player.run();

    // The task should fail with SecurityException (wrapped in RuntimeException by executeTask)
    TaskStatus status = player.getTaskStatuses().get(0);
    assertThrows(SecurityException.class, () -> {
        if (status.getError() != null) {
            throw status.getError();
        }
    });
  }

  @Test
  void testJavaScriptEnabled() throws IOException {
    Agent agent = mock(Agent.class);
    PageDriver driver = mock(PageDriver.class);
    when(agent.getDriver()).thenReturn(driver);
    when(agent.isJavaScriptExecutionEnabled()).thenReturn(true);

    File yamlFile = tempDir.resolve("script_enabled.yaml").toFile();
    try (FileWriter writer = new FileWriter(yamlFile)) {
      writer.write("tasks:\n  - name: test\n    flow:\n      - javascript: \"console.log('hello')\"");
    }

    ScriptPlayer player = new ScriptPlayer(yamlFile.getAbsolutePath(), agent);
    player.run();

    verify(driver, atLeastOnce()).executeScript("console.log('hello')");
  }

  @Test
  void testDangerousJavaScriptBlocked() throws IOException {
    Agent agent = mock(Agent.class);
    when(agent.isJavaScriptExecutionEnabled()).thenReturn(true);

    File yamlFile = tempDir.resolve("script_dangerous.yaml").toFile();
    try (FileWriter writer = new FileWriter(yamlFile)) {
      writer.write("tasks:\n  - name: test\n    flow:\n      - javascript: \"eval('alert(1)')\"");
    }

    ScriptPlayer player = new ScriptPlayer(yamlFile.getAbsolutePath(), agent);
    player.run();

    TaskStatus status = player.getTaskStatuses().get(0);
    assertThrows(SecurityException.class, () -> {
        if (status.getError() != null) {
            throw status.getError();
        }
    });
  }

  @Test
  void testFunctionConstructorBlocked() throws IOException {
    Agent agent = mock(Agent.class);
    when(agent.isJavaScriptExecutionEnabled()).thenReturn(true);

    File yamlFile = tempDir.resolve("script_function.yaml").toFile();
    try (FileWriter writer = new FileWriter(yamlFile)) {
      writer.write("tasks:\n  - name: test\n    flow:\n      - javascript: \"var f = new Function('return 1');\"");
    }

    ScriptPlayer player = new ScriptPlayer(yamlFile.getAbsolutePath(), agent);
    player.run();

    TaskStatus status = player.getTaskStatuses().get(0);
    assertThrows(SecurityException.class, () -> {
        if (status.getError() != null) {
            throw status.getError();
        }
    });
  }
}
