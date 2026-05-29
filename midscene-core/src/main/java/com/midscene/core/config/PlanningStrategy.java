package com.midscene.core.config;

/**
 * Strategy representing how the agent plans its actions: STANDARD represents traditional XML parsing from a standard
 * multimodal LLM prompt. UI_TARS represents specialized coordinate-based vision-language GUI planning. AUTO_GLM
 * represents Auto-GLM coordinate-based pseudo-code GUI planning.
 */
public enum PlanningStrategy {
  STANDARD,
  UI_TARS,
  AUTO_GLM
}
