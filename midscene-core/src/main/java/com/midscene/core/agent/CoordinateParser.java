package com.midscene.core.agent;

import com.midscene.core.pojo.planning.ActionsItem;
import com.midscene.core.pojo.planning.Locate;
import com.midscene.core.pojo.planning.PlanningResponse;
import com.midscene.core.pojo.type.AIActionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses coordinate-based responses from UI-TARS and Auto-GLM models.
 * Normalized coordinates [0, 1000] are mapped to absolute screen pixels.
 */
public class CoordinateParser {

    public static PlanningResponse parseUiTarsResponse(String response, int shotWidth, int shotHeight) {
        PlanningResponse plan = new PlanningResponse();
        plan.setRawResponse(response);

        // Extract Thought
        String thought = extractValue(response, "Thought:");
        if (thought == null) {
            thought = extractTagContent(response, "think");
        }
        plan.setThought(thought != null ? thought : "");
        plan.setLog(thought != null ? thought : "");

        // Clean coordinates: convert <bbox> coordinates to (x, y)
        String cleaned = convertBboxToCoordinates(response);

        // Find action lines
        List<ActionsItem> actions = new ArrayList<>();
        Pattern actionPattern = Pattern.compile("Action:\\s*(\\w+)\\((.*)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = actionPattern.matcher(cleaned);
        
        boolean isFinished = false;
        String finishedMessage = "";

        while (matcher.find()) {
            String actionType = matcher.group(1).toLowerCase();
            String paramsStr = matcher.group(2);
            Map<String, String> params = parseParams(paramsStr);

            ActionsItem action = new ActionsItem();
            switch (actionType) {
                case "click":
                case "tap":
                    action.setType(AIActionType.TAP);
                    setLocateParam(action, params.get("start_box"), shotWidth, shotHeight);
                    actions.add(action);
                    break;
                case "left_double":
                    action.setType(AIActionType.DOUBLE_CLICK);
                    setLocateParam(action, params.get("start_box"), shotWidth, shotHeight);
                    actions.add(action);
                    break;
                case "right_single":
                    action.setType(AIActionType.RIGHT_CLICK);
                    setLocateParam(action, params.get("start_box"), shotWidth, shotHeight);
                    actions.add(action);
                    break;
                case "drag":
                    action.setType(AIActionType.DRAG_AND_DROP);
                    setFromToParams(action, params.get("start_box"), params.get("end_box"), shotWidth, shotHeight);
                    actions.add(action);
                    break;
                case "type":
                    action.setType(AIActionType.INPUT);
                    String content = params.get("content");
                    if (content != null) {
                        if (content.endsWith("\\n")) {
                            content = content.substring(0, content.length() - 2);
                        }
                        action.setValue(content);
                        action.setText(content);
                    }
                    actions.add(action);
                    break;
                case "scroll":
                    action.setType(AIActionType.SCROLL);
                    action.setDirection(params.get("direction"));
                    setLocateParam(action, params.get("start_box"), shotWidth, shotHeight);
                    actions.add(action);
                    break;
                case "hotkey":
                    action.setType(AIActionType.KEYBOARD_PRESS);
                    action.setKeyName(params.get("key"));
                    actions.add(action);
                    break;
                case "wait":
                    action.setType(AIActionType.SLEEP);
                    action.setSleepMs(1000);
                    actions.add(action);
                    break;
                case "finished":
                    isFinished = true;
                    finishedMessage = params.get("content");
                    if (finishedMessage == null || finishedMessage.isEmpty()) {
                        finishedMessage = "Task completed successfully.";
                    }
                    break;
            }
        }

        if (isFinished) {
            plan.setFinalizeSuccess(true);
            plan.setOutput(finishedMessage);
        } else if (!actions.isEmpty()) {
            plan.setActions(actions);
        }

        return plan;
    }

    public static PlanningResponse parseAutoGlmResponse(String response, int shotWidth, int shotHeight) {
        PlanningResponse plan = new PlanningResponse();
        plan.setRawResponse(response);

        // Extract Think
        String think = extractTagContent(response, "think");
        if (think == null) {
            think = extractValue(response, "Think:");
        }
        plan.setThought(think != null ? think : "");
        plan.setLog(think != null ? think : "");

        String answer = extractTagContent(response, "answer");
        if (answer == null) {
            answer = response;
        }

        List<ActionsItem> actions = new ArrayList<>();
        
        if (answer.contains("finish(message=")) {
            int start = answer.indexOf("finish(message=\"");
            if (start != -1) {
                start += 16;
                int end = answer.indexOf("\")", start);
                if (end != -1) {
                    String msg = answer.substring(start, end);
                    plan.setFinalizeSuccess(true);
                    plan.setOutput(msg);
                }
            }
        } else if (answer.contains("do(")) {
            Pattern pattern = Pattern.compile("do\\(action=\"([^\"]+)\"(.*?)\\)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(answer);
            if (matcher.find()) {
                String actionType = matcher.group(1).toLowerCase();
                String paramsStr = matcher.group(2);
                Map<String, String> params = parseParams(paramsStr);

                ActionsItem action = new ActionsItem();
                switch (actionType) {
                    case "tap":
                        action.setType(AIActionType.TAP);
                        setLocateParam(action, params.get("element"), shotWidth, shotHeight);
                        actions.add(action);
                        break;
                    case "double tap":
                        action.setType(AIActionType.DOUBLE_CLICK);
                        setLocateParam(action, params.get("element"), shotWidth, shotHeight);
                        actions.add(action);
                        break;
                    case "long press":
                        action.setType(AIActionType.LONG_PRESS);
                        setLocateParam(action, params.get("element"), shotWidth, shotHeight);
                        actions.add(action);
                        break;
                    case "type":
                    case "type_name":
                        action.setType(AIActionType.INPUT);
                        String text = params.get("text");
                        if (text != null) {
                            action.setValue(text);
                            action.setText(text);
                        }
                        actions.add(action);
                        break;
                    case "swipe":
                        action.setType(AIActionType.SCROLL);
                        setFromToParams(action, params.get("start"), params.get("end"), shotWidth, shotHeight);
                        if (action.getFrom() != null && action.getTo() != null) {
                            int dx = action.getTo().getX() - action.getFrom().getX();
                            int dy = action.getTo().getY() - action.getFrom().getY();
                            if (Math.abs(dy) > Math.abs(dx)) {
                                action.setDirection(dy > 0 ? "up" : "down");
                                action.setDistance(Math.abs(dy));
                            } else {
                                action.setDirection(dx > 0 ? "left" : "right");
                                action.setDistance(Math.abs(dx));
                            }
                        }
                        actions.add(action);
                        break;
                    case "back":
                        action.setType(AIActionType.GO_BACK);
                        actions.add(action);
                        break;
                    case "home":
                        action.setType(AIActionType.GO_BACK);
                        actions.add(action);
                        break;
                    case "wait":
                        action.setType(AIActionType.SLEEP);
                        int sleepMs = 1000;
                        if (params.get("duration") != null) {
                            try {
                                sleepMs = Integer.parseInt(params.get("duration").replaceAll("\\D+", "")) * 1000;
                            } catch (Exception e) {}
                        }
                        action.setSleepMs(sleepMs);
                        actions.add(action);
                        break;
                }
            }
        }

        if (!actions.isEmpty()) {
            plan.setActions(actions);
        }

        return plan;
    }

    private static String extractValue(String text, String key) {
        int idx = text.indexOf(key);
        if (idx == -1) return null;
        int nextLine = text.indexOf('\n', idx);
        if (nextLine == -1) {
            return text.substring(idx + key.length()).trim();
        }
        return text.substring(idx + key.length(), nextLine).trim();
    }

    private static String extractTagContent(String xml, String tagName) {
        Pattern p = Pattern.compile("<" + tagName + "[^>]*>(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher m = p.matcher(xml);
        return m.find() ? m.group(1).trim() : null;
    }

    public static String convertBboxToCoordinates(String text) {
        Pattern pattern = Pattern.compile("<bbox>(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)<\\/bbox>");
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            int x1 = Integer.parseInt(matcher.group(1));
            int y1 = Integer.parseInt(matcher.group(2));
            int x2 = Integer.parseInt(matcher.group(3));
            int y2 = Integer.parseInt(matcher.group(4));
            int x = (x1 + x2) / 2;
            int y = (y1 + y2) / 2;
            matcher.appendReplacement(sb, "(" + x + "," + y + ")");
        }
        matcher.appendTail(sb);
        return sb.toString().replace("[EOS]", "").trim();
    }

    public static Map<String, String> parseParams(String paramsStr) {
        Map<String, String> params = new HashMap<>();
        if (paramsStr == null) return params;
        // Enhanced pattern to match bracketed values like element=[500,600] as a single parameter
        Pattern pattern = Pattern.compile("(\\w+)\\s*=\\s*(?:'([^']*)'|\\\"([^\\\"]*)\\\"|(\\[[^]]*\\]|[^,)]+))");
        Matcher matcher = pattern.matcher(paramsStr);
        while (matcher.find()) {
            String key = matcher.group(1);
            String val = matcher.group(2) != null ? matcher.group(2) :
                         (matcher.group(3) != null ? matcher.group(3) : matcher.group(4));
            if (val != null) {
                params.put(key, val.trim());
            }
        }
        return params;
    }

    private static void setLocateParam(ActionsItem action, String coordStr, int shotWidth, int shotHeight) {
        double[] coords = parseCoordinates(coordStr);
        if (coords != null) {
            Locate locate = new Locate();
            locate.setX((int) Math.round(coords[0] * shotWidth));
            locate.setY((int) Math.round(coords[1] * shotHeight));
            action.setLocate(locate);
        }
    }

    private static void setFromToParams(ActionsItem action, String startStr, String endStr, int shotWidth, int shotHeight) {
        double[] startCoords = parseCoordinates(startStr);
        if (startCoords != null) {
            Locate from = new Locate();
            from.setX((int) Math.round(startCoords[0] * shotWidth));
            from.setY((int) Math.round(startCoords[1] * shotHeight));
            action.setFrom(from);
        }
        double[] endCoords = parseCoordinates(endStr);
        if (endCoords != null) {
            Locate to = new Locate();
            to.setX((int) Math.round(endCoords[0] * shotWidth));
            to.setY((int) Math.round(endCoords[1] * shotHeight));
            action.setTo(to);
        }
    }

    public static double[] parseCoordinates(String coordStr) {
        if (coordStr == null) return null;
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(coordStr);
        List<Integer> nums = new ArrayList<>();
        while (m.find()) {
            nums.add(Integer.parseInt(m.group()));
        }
        if (nums.size() == 2) {
            return new double[]{ nums.get(0) / 1000.0, nums.get(1) / 1000.0 };
        } else if (nums.size() == 4) {
            double x = (nums.get(0) + nums.get(2)) / 2.0;
            double y = (nums.get(1) + nums.get(3)) / 2.0;
            return new double[]{ x / 1000.0, y / 1000.0 };
        }
        return null;
    }

    public static int[] getScreenshotDimensions(String base64Str) {
        try {
            // Remove data URI prefix if present
            if (base64Str.contains(",")) {
                base64Str = base64Str.substring(base64Str.indexOf(",") + 1);
            }
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Str.trim());
            try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageBytes)) {
                java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(bais);
                if (image != null) {
                    return new int[]{ image.getWidth(), image.getHeight() };
                }
            }
        } catch (Exception e) {
            // Log or ignore
        }
        return new int[]{ 1280, 720 }; // Fallback default
    }
}
