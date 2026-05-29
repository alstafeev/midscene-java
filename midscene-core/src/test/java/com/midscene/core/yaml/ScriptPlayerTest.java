package com.midscene.core.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.midscene.core.agent.Agent;
import com.midscene.core.context.Context;
import com.midscene.core.context.ContextEvent;
import com.midscene.core.service.PageDriver;
import dev.langchain4j.model.chat.ChatModel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScriptPlayerTest {

  @TempDir
  Path tempDir;

  private Path createDummyYaml() throws Exception {
    Path yamlFile = tempDir.resolve("test_script.yaml");
    Files.writeString(yamlFile, "tasks: []\n");
    return yamlFile;
  }

  @Test
  void testExecuteFlowItemAliases() throws Exception {
    PageDriver driver = mock(PageDriver.class);
    ChatModel chatModel = mock(ChatModel.class);
    Agent agent = new Agent(driver, chatModel);

    Path yamlPath = createDummyYaml();
    ScriptPlayer player = new ScriptPlayer(yamlPath.toString(), agent);

    // Test ai alias
    YamlFlowItem aiItem = new YamlFlowItem();
    aiItem.setAi("Click the button");
    
    // Test that item type is recognized correctly
    assertEquals(YamlFlowItem.FlowItemType.AI_ACTION, aiItem.getType());
    assertEquals("Click the button", aiItem.getAiActionValue());

    // Test recordToReport alias
    YamlFlowItem recordItem = new YamlFlowItem();
    recordItem.setRecordToReport("Save snapshot");
    assertEquals(YamlFlowItem.FlowItemType.LOG_SCREENSHOT, recordItem.getType());
  }

  @Test
  void testExecuteLogScreenshotRegistersEvent() throws Exception {
    PageDriver driver = mock(PageDriver.class);
    when(driver.getScreenshotBase64()).thenReturn("base64data");
    ChatModel chatModel = mock(ChatModel.class);
    Agent agent = new Agent(driver, chatModel);

    Path yamlPath = createDummyYaml();
    ScriptPlayer player = new ScriptPlayer(yamlPath.toString(), agent);

    YamlFlowItem logItem = new YamlFlowItem();
    logItem.setLogScreenshot("My Title");

    // Invoke the private executeLogScreenshot method using reflection
    java.lang.reflect.Method method = ScriptPlayer.class.getDeclaredMethod("executeLogScreenshot", YamlFlowItem.class);
    method.setAccessible(true);
    method.invoke(player, logItem);

    // Verify screenshot was requested
    verify(driver).getScreenshotBase64();

    // Verify event is in context
    Context context = agent.getContext();
    List<ContextEvent> events = context.getEvents();
    assertEquals(1, events.size());
    ContextEvent event = events.get(0);
    assertEquals("SCREENSHOT_BEFORE", event.getType());
    assertEquals("base64data", event.getScreenshotBase64());
  }
}
