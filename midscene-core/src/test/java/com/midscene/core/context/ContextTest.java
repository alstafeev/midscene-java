package com.midscene.core.context;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ContextTest {

    @Test
    void testLogInstruction() {
        Context context = new Context();
        context.logInstruction("test instruction");

        List<ContextEvent> events = context.getEvents();
        assertEquals(1, events.size());
        ContextEvent event = events.get(0);
        assertEquals("INSTRUCTION", event.getType());
        assertEquals("User Instruction", event.getDescription());
        assertEquals("test instruction", event.getData());
        assertTrue(event.getTimestamp() > 0);
    }

    @Test
    void testLogPlan() {
        Context context = new Context();
        context.logPlan("{\"plan\": \"test\"}");

        List<ContextEvent> events = context.getEvents();
        assertEquals(1, events.size());
        ContextEvent event = events.get(0);
        assertEquals("PLAN", event.getType());
        assertEquals("AI Plan", event.getDescription());
        assertEquals("{\"plan\": \"test\"}", event.getData());
    }

    @Test
    void testLogAction() {
        Context context = new Context();
        context.logAction("click button");

        List<ContextEvent> events = context.getEvents();
        assertEquals(1, events.size());
        ContextEvent event = events.get(0);
        assertEquals("ACTION", event.getType());
        assertEquals("Executing Action", event.getDescription());
        assertEquals("click button", event.getData());
    }

    @Test
    void testLogError() {
        Context context = new Context();
        context.logError("something went wrong");

        List<ContextEvent> events = context.getEvents();
        assertEquals(1, events.size());
        ContextEvent event = events.get(0);
        assertEquals("ERROR", event.getType());
        assertEquals("Error", event.getDescription());
        assertEquals("something went wrong", event.getData());
    }

    @Test
    void testLogScreenshot() {
        Context context = new Context();
        context.logScreenshot("base64data");

        List<ContextEvent> events = context.getEvents();
        assertEquals(1, events.size());
        ContextEvent event = events.get(0);
        assertEquals("SCREENSHOT", event.getType());
        assertEquals("Screenshot captured", event.getDescription());
        assertEquals("base64data", event.getScreenshotBase64());
    }

    @Test
    void testLogEventDirectly() {
        Context context = new Context();
        ContextEvent event = ContextEvent.builder()
                .type("CUSTOM")
                .description("Custom Event")
                .build();
        context.logEvent(event);

        assertEquals(1, context.getEvents().size());
        assertEquals(event, context.getEvents().get(0));
    }
}
