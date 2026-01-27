package com.midscene.core.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.midscene.core.pojo.planning.PlanningResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;

/**
 * Cache system for storing and retrieving AI planning responses. Supports both in-memory and file-based caching modes.
 */
@Log4j2
public class TaskCache {

  private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = JsonMapper.builder()
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .build();
  private final Map<String, PlanningResponse> memoryCache = new ConcurrentHashMap<>();
  private CacheMode mode;
  private Path cacheFilePath;
  private final Object fileLock = new Object();

  /**
   * Creates a new TaskCache with the given mode and optional file path.
   *
   * @param mode          the cache mode
   * @param cacheFilePath optional path to persist cache to file (can be null for memory-only)
   */
  public TaskCache(CacheMode mode, Path cacheFilePath) {
    this.mode = mode;
    this.cacheFilePath = cacheFilePath;

    if (cacheFilePath != null && mode != CacheMode.DISABLED) {
      loadFromFile();
    }
  }

  /**
   * Creates a new memory-only cache with READ_WRITE mode.
   */
  public TaskCache() {
    this(CacheMode.READ_WRITE, null);
  }

  /**
   * Creates a cache with a specific file path for persistence.
   *
   * @param cacheFilePath path to the cache file
   * @return a new TaskCache instance
   */
  public static TaskCache withFile(Path cacheFilePath) {
    return new TaskCache(CacheMode.READ_WRITE, cacheFilePath);
  }

  /**
   * Creates a cache with a specific file path and mode.
   *
   * @param cacheFilePath path to the cache file
   * @param mode          the cache mode
   * @return a new TaskCache instance
   */
  public static TaskCache withFile(Path cacheFilePath, CacheMode mode) {
    return new TaskCache(mode, cacheFilePath);
  }

  /**
   * Creates a memory-only cache.
   *
   * @return a new TaskCache instance
   */
  public static TaskCache memoryOnly() {
    return new TaskCache(CacheMode.READ_WRITE, null);
  }

  /**
   * Creates a disabled cache (no caching).
   *
   * @return a new TaskCache instance that never caches
   */
  public static TaskCache disabled() {
    return new TaskCache(CacheMode.DISABLED, null);
  }

  /**
   * Gets a cached planning response for the given prompt.
   *
   * @param prompt the prompt to look up
   * @return the cached response, or null if not found or reading is disabled
   */
  public PlanningResponse get(String prompt) {
    if (mode == CacheMode.WRITE_ONLY || mode == CacheMode.DISABLED) {
      return null;
    }

    String key = generateCacheKey(prompt);
    PlanningResponse cached = memoryCache.get(key);

    if (cached != null) {
      log.debug("Cache hit for prompt key: {}", key.substring(0, 8));
    }

    return cached;
  }

  /**
   * Stores a planning response in the cache.
   *
   * @param prompt   the prompt used to generate the response
   * @param response the response to cache
   */
  public void put(String prompt, PlanningResponse response) {
    if (mode == CacheMode.READ_ONLY || mode == CacheMode.DISABLED) {
      return;
    }

    String key = generateCacheKey(prompt);
    memoryCache.put(key, response);
    log.debug("Cached response for prompt key: {}", key.substring(0, 8));

    if (cacheFilePath != null) {
      appendToFile(key, response);
    }
  }

  /**
   * Checks if a response is cached for the given prompt.
   *
   * @param prompt the prompt to check
   * @return true if cached, false otherwise
   */
  public boolean contains(String prompt) {
    if (mode == CacheMode.WRITE_ONLY || mode == CacheMode.DISABLED) {
      return false;
    }
    return memoryCache.containsKey(generateCacheKey(prompt));
  }

  /**
   * Clears all cached entries.
   */
  public void clear() {
    memoryCache.clear();
    log.info("Cache cleared");

    if (cacheFilePath != null) {
      saveToFile();
    }
  }

  /**
   * Invalidates (removes) a specific cached entry.
   * Use this when a cached plan fails and needs to be refreshed.
   *
   * @param prompt the prompt to invalidate
   * @return true if the entry was removed, false if it wasn't cached
   */
  public boolean invalidate(String prompt) {
    if (mode == CacheMode.DISABLED) {
      return false;
    }

    String key = generateCacheKey(prompt);
    PlanningResponse removed = memoryCache.remove(key);
    
    if (removed != null) {
      log.info("Invalidated cache entry for prompt key: {}", key.substring(0, 8));
      if (cacheFilePath != null) {
        saveToFile();
      }
      return true;
    }
    return false;
  }

  /**
   * Returns the number of cached entries.
   *
   * @return cache size
   */
  public int size() {
    return memoryCache.size();
  }

  /**
   * Gets the current cache mode.
   *
   * @return the cache mode
   */
  public CacheMode getMode() {
    return mode;
  }

  /**
   * Sets the cache mode.
   *
   * @param mode the new cache mode
   */
  public void setMode(CacheMode mode) {
    this.mode = mode;
  }

  /**
   * Generates a cache key from a prompt using SHA-256 hash.
   *
   * @param prompt the prompt to hash
   * @return the hash key
   */
  private String generateCacheKey(String prompt) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(prompt.getBytes());
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      // Fallback to simple hash
      log.warn("SHA-256 not available, using hashCode fallback");
      return String.valueOf(prompt.hashCode());
    }
  }

  /**
   * Loads cache entries from file.
   */
  private void loadFromFile() {
    if (cacheFilePath == null || !Files.exists(cacheFilePath)) {
      return;
    }

    try {
      if (Files.size(cacheFilePath) == 0) {
        return;
      }

      try (com.fasterxml.jackson.core.JsonParser parser = MAPPER.createParser(cacheFilePath.toFile())) {
        MappingIterator<Map<String, PlanningResponse>> it = MAPPER.readValues(
            parser, new TypeReference<Map<String, PlanningResponse>>() {
            });
        while (it.hasNext()) {
          Map<String, PlanningResponse> batch = it.next();
          memoryCache.putAll(batch);
        }
      }
      log.info("Loaded {} cache entries from {}", memoryCache.size(), cacheFilePath);
    } catch (IOException e) {
      log.warn("Failed to load cache from file: {}", e.getMessage());
    }
  }

  /**
   * Appends a single cache entry to the file.
   */
  private void appendToFile(String key, PlanningResponse response) {
    if (cacheFilePath == null) {
      return;
    }

    synchronized (fileLock) {
      try {
        // Ensure parent directory exists
        Path parent = cacheFilePath.getParent();
        if (parent != null && !Files.exists(parent)) {
          Files.createDirectories(parent);
        }

        // We write a minimal JSON object for this entry
        Map<String, PlanningResponse> entry = Collections.singletonMap(key, response);
        String json = MAPPER.writeValueAsString(entry);

        Files.writeString(cacheFilePath, json + System.lineSeparator(),
            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      } catch (IOException e) {
        log.warn("Failed to append cache to file: {}", e.getMessage());
      }
    }
  }

  /**
   * Saves (rewrites) all cache entries to file.
   */
  private void saveToFile() {
    if (cacheFilePath == null) {
      return;
    }

    synchronized (fileLock) {
      try {
        // Ensure parent directory exists
        Path parent = cacheFilePath.getParent();
        if (parent != null && !Files.exists(parent)) {
          Files.createDirectories(parent);
        }

        String json = MAPPER.writerWithDefaultPrettyPrinter()
            .writeValueAsString(memoryCache);
        Files.writeString(cacheFilePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.debug("Saved {} cache entries to {}", memoryCache.size(), cacheFilePath);
      } catch (IOException e) {
        log.warn("Failed to save cache to file: {}", e.getMessage());
      }
    }
  }

  /**
   * Cache mode determining how the cache operates.
   */
  public enum CacheMode {
    /**
     * Read from cache only, never write.
     */
    READ_ONLY,
    /**
     * Write to cache only, never read.
     */
    WRITE_ONLY,
    /**
     * Read and write to cache.
     */
    READ_WRITE,
    /**
     * Cache is disabled.
     */
    DISABLED
  }
}
