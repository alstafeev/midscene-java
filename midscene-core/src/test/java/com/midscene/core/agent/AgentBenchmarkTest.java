package com.midscene.core.agent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.midscene.core.cache.TaskCache;
import com.midscene.core.model.AIModel;
import com.midscene.core.pojo.options.WaitOptions;
import com.midscene.core.service.PageDriver;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class AgentBenchmarkTest {

  @Test
  public void testAiWaitForPerformance() {
    // Mock dependencies
    PageDriver driver = mock(PageDriver.class);
    AIModel aiModel = mock(AIModel.class);

    // Setup Agent
    // Agent constructor creates an Orchestrator which uses the AIModel
    // We need to ensure the AIModel mock is used.
    // Agent has a constructor public Agent(PageDriver driver, AIModel aiModel)
    Agent agent = new Agent(driver, aiModel);

    // Mock PageDriver screenshot behavior (needed for Planner.query)
    when(driver.getScreenshotBase64()).thenReturn("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    // Mock executeScript to simulate mutations
    // 1. Init script (any string starting with window.__midscene_mutation_happened = true) -> return null
    // 2. Check script -> return sequence: true (initial), false (wait), true (mutation)
    when(driver.executeScript(anyString())).thenAnswer(invocation -> {
      String script = invocation.getArgument(0);
      if (script.contains("if (!window.__midscene_observer)")) {
        return null;
      }
      if (script.contains("return happened")) {
         // simulate:
         // call 1: true (initial check)
         // call 2: false (no mutation)
         // call 3: true (mutation happened)
         // call 4: ...
         return invocation.getMock().toString().contains("call_1") ? true : false;
         // Wait, Mockito state is hard to track this way inside the answer without external counter.
         // Let's use a counter.
      }
      return null;
    });

    // Refined mock for executeScript with counter
    final int[] scriptCallCount = {0};
    when(driver.executeScript(anyString())).thenAnswer(invocation -> {
        String script = invocation.getArgument(0);
        if (script.contains("if (!window.__midscene_observer)")) {
            return null;
        }
        if (script.contains("return happened")) {
            scriptCallCount[0]++;
            if (scriptCallCount[0] == 1) return true; // Initial check
            if (scriptCallCount[0] == 2) return false; // No mutation
            if (scriptCallCount[0] == 3) return true; // Mutation happened
            return false;
        }
        return null;
    });

    // Mock AIModel behavior
    // aiBoolean calls aiQuery which calls model.chat(messages)
    // We need to mock chat behavior.
    // Orchestrator logic might wrap the message.
    // However, aiBoolean expects "true" or "false" in the response.

    // Scenario:
    // Iteration 1: executeScript(check) -> true. AI Called. Response: false.
    // Iteration 2: executeScript(check) -> false. AI Skipped.
    // Iteration 3: executeScript(check) -> true. AI Called. Response: true.
    // Total AI calls: 2

    when(aiModel.chat(any())).thenAnswer(new Answer<ChatResponse>() {
      private int count = 0;

      @Override
      public ChatResponse answer(InvocationOnMock invocation) {
        count++;
        String responseText = "false";
        // If count is 2, it means it's the second call to AI.
        // In our scenario, the 2nd call happens when mutation occurs and condition is true.
        if (count >= 2) {
          responseText = "true";
        }
        return ChatResponse.builder()
            .aiMessage(AiMessage.from(responseText))
            .build();
      }
    });

    long start = System.currentTimeMillis();

    // Set check interval to a small value to make test fast but still loop
    WaitOptions options = WaitOptions.builder()
        .timeoutMs(2000)
        .checkIntervalMs(50)
        .build();

    agent.aiWaitFor("some condition", options);

    long duration = System.currentTimeMillis() - start;
    System.out.println("Duration: " + duration + "ms");

    // Verify AIModel was called 2 times (instead of 3 in the baseline scenario if we polled every time)
    // Baseline would be:
    // 1. check -> true (false)
    // 2. check -> false (false) - but polling would call AI
    // 3. check -> true (true)

    // With optimization:
    // 1. check -> true (mutation happened/init) -> AI call (false)
    // 2. check -> false (no mutation) -> Skip AI
    // 3. check -> true (mutation happened) -> AI call (true) -> return

    verify(aiModel, times(2)).chat(any());
  }
}
