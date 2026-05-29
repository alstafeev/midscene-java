package com.midscene.core.agent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import com.midscene.core.config.MidsceneConfig;
import com.midscene.core.config.ModelProvider;
import com.midscene.core.context.Context;
import com.midscene.core.service.PageDriver;
import org.junit.jupiter.api.Test;

class AgentTest {

  @Test
  void testGetContext() {
    PageDriver driver = mock(PageDriver.class);
    MidsceneConfig config = MidsceneConfig.builder()
        .provider(ModelProvider.OPENAI)
        .apiKey("key")
        .build();

    Agent agent = Agent.create(config, driver);
    Context context = agent.getContext();

    assertNotNull(context);
  }

  @Test
  void testSetCachePropagates() throws Exception {
    PageDriver driver = mock(PageDriver.class);
    MidsceneConfig config = MidsceneConfig.builder()
        .provider(ModelProvider.OPENAI)
        .apiKey("key")
        .build();

    Agent agent = Agent.create(config, driver);
    com.midscene.core.cache.TaskCache newCache = com.midscene.core.cache.TaskCache.memoryOnly();
    
    agent.setCache(newCache);
    
    // Verify Agent's cache is updated
    org.junit.jupiter.api.Assertions.assertSame(newCache, agent.getCache());

    // Verify Orchestrator's Planner's cache is also updated using reflection
    java.lang.reflect.Field orchestratorField = Agent.class.getDeclaredField("orchestrator");
    orchestratorField.setAccessible(true);
    Orchestrator orchestrator = (Orchestrator) orchestratorField.get(agent);

    java.lang.reflect.Field plannerField = Orchestrator.class.getDeclaredField("planner");
    plannerField.setAccessible(true);
    Planner planner = (Planner) plannerField.get(orchestrator);

    java.lang.reflect.Field cacheField = Planner.class.getDeclaredField("cache");
    cacheField.setAccessible(true);
    com.midscene.core.cache.TaskCache plannerCache = (com.midscene.core.cache.TaskCache) cacheField.get(planner);

    org.junit.jupiter.api.Assertions.assertSame(newCache, plannerCache);
  }
}
