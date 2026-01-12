package com.midscene.core.config;

public enum ModelProvider {
  OPENAI("gpt-4o", "https://api.openai.com/v1"),
  GEMINI("gemini-3-pro-preview", "https://generativelanguage.googleapis.com/v1beta");

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
