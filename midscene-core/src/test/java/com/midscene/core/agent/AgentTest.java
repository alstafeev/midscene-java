package com.midscene.core.agent;

import com.midscene.core.config.MidsceneConfig;
import com.midscene.core.config.ModelProvider;
import com.midscene.core.context.Context;
import com.midscene.core.service.PageDriver;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
}
