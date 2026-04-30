package com.midscene.core.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityReproductionTest {

    @TempDir
    Path tempDir;

    @Test
    public void testPathTraversal() throws IOException {
        // This test demonstrates that TaskCache should block paths outside the intended directory.
        // We use an absolute path that is definitely outside the project and temp directory.
        Path outsidePath = Paths.get("/outside_cache_test.json");

        // Verification: it should throw a SecurityException
        assertThrows(SecurityException.class, () -> {
            TaskCache.withFile(outsidePath);
        });
    }

    @Test
    public void testLegitimatePath() throws IOException {
        // This test ensures that legitimate paths within the current directory are allowed.
        Path relativePath = Paths.get("cache_test.json");
        TaskCache cache = TaskCache.withFile(relativePath);
        assertEquals(relativePath, cache.getCacheFilePath());
    }

    @Test
    public void testTempDirPath() throws IOException {
        // This test ensures that paths within the system temp directory are allowed.
        Path tempFilePath = Paths.get(System.getProperty("java.io.tmpdir"), "cache_test.json");
        TaskCache cache = TaskCache.withFile(tempFilePath);
        assertEquals(tempFilePath, cache.getCacheFilePath());
    }
}
