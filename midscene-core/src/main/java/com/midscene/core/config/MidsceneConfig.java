package com.midscene.core.config;

public class MidsceneConfig {

  private final ModelProvider provider;
  private final String apiKey;
  private final String modelName;
  private final String baseUrl;
  private final long timeoutMs;

  private MidsceneConfig(Builder builder) {
    this.provider = builder.provider;
    this.apiKey = builder.apiKey;
    this.modelName = builder.modelName;
    this.baseUrl = builder.baseUrl;
    this.timeoutMs = builder.timeoutMs;
  }

  public static Builder builder() {
    return new Builder();
  }

  public ModelProvider getProvider() {
    return provider;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getModelName() {
    return modelName;
  }

  public long getTimeoutMs() {
    return timeoutMs;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public static class Builder {

    private ModelProvider provider = ModelProvider.OPENAI;
    private String apiKey;
    private String modelName;
    private String baseUrl;
    private long timeoutMs = 30000; // Default 30s

    public Builder provider(ModelProvider provider) {
      this.provider = provider;
      return this;
    }

    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder modelName(String modelName) {
      this.modelName = modelName;
      return this;
    }

    public Builder timeoutMs(long timeoutMs) {
      this.timeoutMs = timeoutMs;
      return this;
    }

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public MidsceneConfig build() {
      if (apiKey == null || apiKey.isEmpty()) {
        throw new IllegalArgumentException("API Key must be provided");
      }
      if (modelName == null || modelName.isEmpty()) {
        modelName = provider.getModelName();
      }
      if (baseUrl == null || baseUrl.isEmpty()) {
        baseUrl = provider.getBaseUrl();
      }
      return new MidsceneConfig(this);
    }
  }
}
