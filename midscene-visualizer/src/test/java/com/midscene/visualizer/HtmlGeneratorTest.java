package com.midscene.visualizer;

import com.midscene.core.context.Context;
import com.midscene.core.context.ContextEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HtmlGeneratorTest {

    @Test
    void testGenerateEmptyContext() {
        Context context = new Context();
        String html = HtmlGenerator.generate(context);
        assertNotNull(html);
        assertTrue(html.contains("Midscene Execution Report"));
        assertTrue(html.contains("<table>"));
    }

    @Test
    void testGenerateWithEvents() {
        Context context = new Context();
        context.logInstruction("test instruction");
        context.logScreenshot("base64data");

        String html = HtmlGenerator.generate(context);
        assertTrue(html.contains("test instruction"));
        assertTrue(html.contains("base64data"));
        assertTrue(html.contains("INSTRUCTION"));
        assertTrue(html.contains("SCREENSHOT"));
    }

    @Test
    void testEscapeHtml() {
        Context context = new Context();
        context.logInstruction("<script>alert('xss')</script>");

        String html = HtmlGenerator.generate(context);
        assertTrue(html.contains("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"));
    }

    @Test
    void testNullData() {
        Context context = new Context();
        ContextEvent event = ContextEvent.builder().type("TEST").build();
        context.logEvent(event);

        String html = HtmlGenerator.generate(context);
        assertNotNull(html);
    }
}
