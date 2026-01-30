package com.midscene.core.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.midscene.core.pojo.planning.PlanningResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TaskCacheTest {

    @TempDir
    Path tempDir;

    @Test
    public void testPersistence() {
        Path cacheFile = tempDir.resolve("cache.json");
        TaskCache cache = TaskCache.withFile(cacheFile);

        String prompt = "test_prompt";
        PlanningResponse response = new PlanningResponse();
        response.setLog("test log");

        cache.put(prompt, response);
        Assertions.assertTrue(cache.contains(prompt));

        // Create new instance to simulate restart
        TaskCache cache2 = TaskCache.withFile(cacheFile);
        Assertions.assertTrue(cache2.contains(prompt));
        Assertions.assertEquals("test log", cache2.get(prompt).getLog());
    }

    @Test
    public void testAppendOnlyFormat() throws IOException {
        Path cacheFile = tempDir.resolve("append_cache.json");
        TaskCache cache = TaskCache.withFile(cacheFile);

        cache.put("p1", new PlanningResponse());
        cache.put("p2", new PlanningResponse());

        // Check file content
        String content = Files.readString(cacheFile);
        // Should contain two JSON objects, likely on separate lines or just concatenated
        // Since we use append with line separator
        long lines = Files.lines(cacheFile).count();
        Assertions.assertEquals(2, lines);
    }

    @Test
    public void testLegacyFormatCompatibility() throws IOException {
        Path cacheFile = tempDir.resolve("legacy_cache.json");

        // Manually write legacy format (single map)
        ObjectMapper mapper = new ObjectMapper();
        Map<String, PlanningResponse> data = Map.of(
            "legacy_key", new PlanningResponse()
        );
        String legacyJson = mapper.writeValueAsString(data);
        Files.writeString(cacheFile, legacyJson);

        // Load with TaskCache
        TaskCache cache = TaskCache.withFile(cacheFile);
        // We cannot check contains("legacy_key") because that would check hash("legacy_key")
        // But we can verify the size is 1, meaning it loaded the entry.
        Assertions.assertEquals(1, cache.size());
    }

    @Test
    public void testInvalidateRewritesFile() throws IOException {
        Path cacheFile = tempDir.resolve("invalidate_cache.json");
        TaskCache cache = TaskCache.withFile(cacheFile);

        cache.put("p1", new PlanningResponse());
        cache.put("p2", new PlanningResponse());

        long linesBefore = Files.lines(cacheFile).count();
        Assertions.assertEquals(2, linesBefore);

        cache.invalidate("p1");

        // After invalidate, it should have rewritten the file.
        // It might be one line (legacy format used pretty printer? No, I used default pretty printer in saveToFile).
        // If pretty printer is used, it's multiple lines.
        // But let's check content.
        TaskCache cache2 = TaskCache.withFile(cacheFile);
        Assertions.assertEquals(1, cache2.size());
        Assertions.assertFalse(cache2.contains("p1"));
        Assertions.assertTrue(cache2.contains("p2"));
    }

    @Test
    public void testClearTruncatesFile() throws IOException {
        Path cacheFile = tempDir.resolve("clear_cache.json");
        TaskCache cache = TaskCache.withFile(cacheFile);

        cache.put("p1", new PlanningResponse());
        cache.clear();

        // File should contain empty JSON object "{}" which is not 0 bytes
        String content = Files.readString(cacheFile).trim().replace(" ", "").replace("\n", "").replace("\r", "");
        Assertions.assertEquals("{}", content);
        Assertions.assertEquals(0, cache.size());

        TaskCache cache2 = TaskCache.withFile(cacheFile);
        Assertions.assertEquals(0, cache2.size());
    }
}
