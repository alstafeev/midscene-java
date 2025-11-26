package com.midscene.core.context;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContextEventTest {

    @Test
    void testBuilderAndGetters() {
        long now = System.currentTimeMillis();
        ContextEvent event = ContextEvent.builder()
                .type("TEST")
                .description("Test Description")
                .data("Test Data")
                .screenshotBase64("base64")
                .timestamp(now)
                .build();

        assertEquals("TEST", event.getType());
        assertEquals("Test Description", event.getDescription());
        assertEquals("Test Data", event.getData());
        assertEquals("base64", event.getScreenshotBase64());
        assertEquals(now, event.getTimestamp());
    }

    @Test
    void testToString() {
        ContextEvent event = ContextEvent.builder().type("TEST").build();
        assertNotNull(event.toString());
    }

    @Test
    void testEqualsAndHashCode() {
        ContextEvent event1 = ContextEvent.builder().type("TEST").build();
        ContextEvent event2 = ContextEvent.builder().type("TEST").build();

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
