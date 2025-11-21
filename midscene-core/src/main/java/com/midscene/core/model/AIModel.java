package com.midscene.core.model;

import dev.langchain4j.data.message.ChatMessage;
import java.util.List;

public interface AIModel {

  String chat(List<ChatMessage> messages);
}
