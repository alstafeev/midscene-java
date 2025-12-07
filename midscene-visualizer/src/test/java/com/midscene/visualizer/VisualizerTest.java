package com.midscene.visualizer;

import com.midscene.core.context.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class VisualizerTest {

    @Test
    void testGenerateReport(@TempDir Path tempDir) throws Exception {
        Context context = new Context();
        context.logInstruction("test");

        Path reportPath = tempDir.resolve("report.html");
        Visualizer.generateReport(context, reportPath);

        assertTrue(Files.exists(reportPath));
        String content = Files.readString(reportPath);
        assertTrue(content.contains("Midscene Report"));
        assertTrue(content.contains("midscene_web_dump"));
    }

    @Test
    void testGenerateReportError(@TempDir Path tempDir) {
        Context context = new Context();
        // Trying to write to a directory instead of a file should fail
        assertThrows(RuntimeException.class, () -> {
            Visualizer.generateReport(context, tempDir);
        });
    }
}
