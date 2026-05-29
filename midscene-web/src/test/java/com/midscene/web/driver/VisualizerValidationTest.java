package com.midscene.web.driver;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VisualizerValidationTest {

    @Test
    public void validateReportUI() {
        Path reportPath = Path.of("../midscene-visualizer/target/sample_report.html").toAbsolutePath().normalize();
        assertTrue(Files.exists(reportPath), "Sample report must exist at: " + reportPath);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            
            page.onConsoleMessage(msg -> {
                System.out.println("Console [" + msg.type() + "]: " + msg.text() + " at " + msg.location());
            });

            page.onPageError(error -> {
                System.err.println("Page JS Error: " + error);
            });

            page.onRequest(request -> System.out.println("Network Request: " + request.url()));
            page.onResponse(response -> System.out.println("Network Response: " + response.url() + " Status: " + response.status()));
            page.onRequestFailed(request -> System.out.println("Network Request Failed: " + request.url() + " Error: " + request.failure()));


            // Navigate using file protocol
            String fileUrl = reportPath.toUri().toString();
            System.out.println("Navigating to: " + fileUrl);
            page.navigate(fileUrl);

            // Wait for the page to load and React app to mount
            page.waitForLoadState(LoadState.LOAD);
            
            // Wait 3 seconds for React rendering/animations
            page.waitForTimeout(3000);

            String title = page.title();
            System.out.println("Page Title: " + title);
            assertTrue(title.contains("Midscene") || title.contains("Report"), "Title should contain 'Midscene' or 'Report'");

            // Capture a screenshot of the rendered visualizer to verify visual correctness
            Path screenshotPath = Path.of("target/visualizer_rendered.png");
            page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath).setFullPage(true));
            System.out.println("Captured visualizer screenshot at: " + screenshotPath.toAbsolutePath());
            assertTrue(Files.exists(screenshotPath));
            
            browser.close();
        }
    }
}
