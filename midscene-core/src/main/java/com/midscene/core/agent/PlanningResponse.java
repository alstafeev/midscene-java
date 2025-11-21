package com.midscene.core.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanningResponse {

  public List<PlanningAction> actions;
  public boolean more_actions_needed_by_instruction;
  public String error;
}
