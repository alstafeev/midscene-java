package com.midscene.core.agent;

import com.midscene.core.pojo.planning.ActionsItem;
import com.midscene.core.pojo.planning.PlanningResponse;
import com.midscene.core.pojo.type.AIActionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CoordinateParserTest {

    @Test
    public void testConvertBboxToCoordinates() {
        String input = "Thought: I need to click the button.\n" +
                "Action: click(start_box='<bbox>100 200 300 400</bbox>')";
        String expected = "Thought: I need to click the button.\n" +
                "Action: click(start_box='(200,300)')";
        assertEquals(expected, CoordinateParser.convertBboxToCoordinates(input));
    }

    @Test
    public void testParseCoordinates() {
        double[] coords = CoordinateParser.parseCoordinates("(460,452)");
        assertNotNull(coords);
        assertEquals(0.460, coords[0], 0.001);
        assertEquals(0.452, coords[1], 0.001);

        double[] bboxCoords = CoordinateParser.parseCoordinates("[100, 200, 300, 400]");
        assertNotNull(bboxCoords);
        assertEquals(0.200, bboxCoords[0], 0.001);
        assertEquals(0.300, bboxCoords[1], 0.001);
    }

    @Test
    public void testParseUiTarsResponse() {
        String response = "Thought: Click the button\n" +
                "Action: click(start_box='(200,300)')";
        PlanningResponse plan = CoordinateParser.parseUiTarsResponse(response, 1000, 1000);
        
        assertNotNull(plan);
        assertEquals("Click the button", plan.getThought());
        assertFalse(plan.getActions().isEmpty());
        
        ActionsItem action = plan.getActions().get(0);
        assertEquals(AIActionType.TAP, action.getType());
        assertNotNull(action.getLocate());
        assertEquals(200, action.getLocate().getX());
        assertEquals(300, action.getLocate().getY());
    }

    @Test
    public void testParseUiTarsDragResponse() {
        String response = "Thought: Drag from folder to trash\n" +
                "Action: drag(start_box='(100,200)', end_box='(300,400)')";
        PlanningResponse plan = CoordinateParser.parseUiTarsResponse(response, 1000, 1000);
        
        assertNotNull(plan);
        assertFalse(plan.getActions().isEmpty());
        
        ActionsItem action = plan.getActions().get(0);
        assertEquals(AIActionType.DRAG_AND_DROP, action.getType());
        assertNotNull(action.getFrom());
        assertEquals(100, action.getFrom().getX());
        assertEquals(200, action.getFrom().getY());
        assertNotNull(action.getTo());
        assertEquals(300, action.getTo().getX());
        assertEquals(400, action.getTo().getY());
    }

    @Test
    public void testParseUiTarsTypeAndFinishedResponse() {
        String responseType = "Thought: Type my user name\n" +
                "Action: type(content='user1\\n')";
        PlanningResponse planType = CoordinateParser.parseUiTarsResponse(responseType, 1000, 1000);
        assertNotNull(planType);
        ActionsItem action = planType.getActions().get(0);
        assertEquals(AIActionType.INPUT, action.getType());
        assertEquals("user1", action.getValue());

        String responseFinish = "Thought: I am done\n" +
                "Action: finished(content='Success!')";
        PlanningResponse planFinish = CoordinateParser.parseUiTarsResponse(responseFinish, 1000, 1000);
        assertNotNull(planFinish);
        assertTrue(planFinish.getFinalizeSuccess());
        assertEquals("Success!", planFinish.getOutput());
    }

    @Test
    public void testParseAutoGlmResponse() {
        String response = "<think>点击搜索框</think>\n" +
                "<answer>do(action=\"Tap\", element=[500,600])</answer>";
        PlanningResponse plan = CoordinateParser.parseAutoGlmResponse(response, 1280, 720);
        
        assertNotNull(plan);
        assertEquals("点击搜索框", plan.getThought());
        assertFalse(plan.getActions().isEmpty());
        
        ActionsItem action = plan.getActions().get(0);
        assertEquals(AIActionType.TAP, action.getType());
        assertNotNull(action.getLocate());
        assertEquals((int)Math.round(0.5 * 1280), action.getLocate().getX());
        assertEquals((int)Math.round(0.6 * 720), action.getLocate().getY());
    }

    @Test
    public void testParseAutoGlmFinishResponse() {
        String response = "<think>任务结束</think>\n" +
                "<answer>finish(message=\"Mission accomplished\")</answer>";
        PlanningResponse plan = CoordinateParser.parseAutoGlmResponse(response, 1000, 1000);
        
        assertNotNull(plan);
        assertTrue(plan.getFinalizeSuccess());
        assertEquals("Mission accomplished", plan.getOutput());
    }

    @Test
    public void testGetScreenshotDimensions() {
        // 1x1 red PNG
        String validPng = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";
        int[] dims = CoordinateParser.getScreenshotDimensions(validPng);
        assertNotNull(dims);
        assertEquals(2, dims.length);
        assertEquals(1, dims[0]);
        assertEquals(1, dims[1]);

        // With data URI prefix
        String validPngDataUri = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";
        int[] dimsDataUri = CoordinateParser.getScreenshotDimensions(validPngDataUri);
        assertNotNull(dimsDataUri);
        assertEquals(1, dimsDataUri[0]);
        assertEquals(1, dimsDataUri[1]);

        // Invalid base64 fallback
        int[] dimsFallback = CoordinateParser.getScreenshotDimensions("invalid_base64_string_here");
        assertNotNull(dimsFallback);
        assertEquals(1280, dimsFallback[0]);
        assertEquals(720, dimsFallback[1]);
    }
}
