package com.midscene.core.utils;

import org.junit.jupiter.api.Test;
import lombok.Data;
import static org.junit.jupiter.api.Assertions.*;

class JsonResponseToPojoMapperTest {

    @Data
    static class TestPojo {
        private String name;
        private int value;
    }

    @Test
    void testMapSimpleJson() {
        String json = "{\"name\": " + "\"test\", \"value\": 123}";
        TestPojo result = JsonResponseToPojoMapper.mapResponseToClass(json, TestPojo.class);
        assertEquals("test", result.getName());
        assertEquals(123, result.getValue());
    }

    @Test
    void testMapMarkdownJson() {
        String json = "```json\n{\"name\": " + "\"test\", \"value\": 123}\n```";
        TestPojo result = JsonResponseToPojoMapper.mapResponseToClass(json, TestPojo.class);
        assertEquals("test", result.getName());
        assertEquals(123, result.getValue());
    }

    @Test
    void testCaseInsensitive() {
        String json = "{\"NAME\": " + "\"test\", \"VALUE\": 123}";
        TestPojo result = JsonResponseToPojoMapper.mapResponseToClass(json, TestPojo.class);
        assertEquals("test", result.getName());
        assertEquals(123, result.getValue());
    }
}
