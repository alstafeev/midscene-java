package com.midscene.core.yaml;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.midscene.core.agent.Agent;
import com.midscene.core.cache.TaskCache;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

class ScriptPlayerSecurityTest {

    @TempDir
    Path tempDir;

    @Test
    void testCacheIdPathTraversalFix() throws IOException, NoSuchFieldException, IllegalAccessException {
        Agent agent = mock(Agent.class);

        // Create a YAML with a malicious cache ID
        String maliciousId = "../outside_cache";
        File yamlFile = tempDir.resolve("malicious_script.yaml").toFile();
        try (FileWriter writer = new FileWriter(yamlFile)) {
            writer.write("agent:\n" +
                         "  cache:\n" +
                         "    id: \"" + maliciousId + "\"\n" +
                         "    strategy: \"read-write\"\n" +
                         "tasks: []");
        }

        // Create ScriptPlayer
        new ScriptPlayer(yamlFile.getAbsolutePath(), agent);

        // Capture the TaskCache passed to agent.setCache
        ArgumentCaptor<TaskCache> cacheCaptor = ArgumentCaptor.forClass(TaskCache.class);
        verify(agent, atLeastOnce()).setCache(cacheCaptor.capture());

        TaskCache capturedCache = cacheCaptor.getValue();
        assertNotNull(capturedCache);

        // Use reflection to check the private cacheFilePath in TaskCache
        Field field = TaskCache.class.getDeclaredField("cacheFilePath");
        field.setAccessible(true);
        Path cachePath = (Path) field.get(capturedCache);

        // After fix, it should be "outside_cache.cache.json"
        assertFalse(cachePath.toString().contains(".."), "Path should not contain '..'");
        assertEquals("outside_cache.cache.json", cachePath.toString(), "Path should be sanitized to filename only");
    }
}
