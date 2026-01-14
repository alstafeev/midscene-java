package com.midscene.core.config;

public enum ModelProvider {
  OPENAI("gpt-4o", "https://api.openai.com/v1"),
  GEMINI("gemini-3-pro-preview", "https://generativelanguage.googleapis.com/v1beta"),
  ANTHROPIC("claude-3-5-sonnet-20240620", "https://api.anthropic.com/v1/"),
  MISTRAL("small-latest", "https://api.mistral.ai/v1"),
  AZURE_OPEN_AI("gpt-4o", "https://openai.azure.com/"),
  OLLAMA("llama3.1", "http://localhost:11434/");

  private final String modelName;
  private final String baseUrl;

  ModelProvider(String modelName, String baseUrl) {
    this.modelName = modelName;
    this.baseUrl = baseUrl;
  }

  public String getModelName() {
    return modelName;
  }

  public String getBaseUrl() {
    return baseUrl;
  }
}
