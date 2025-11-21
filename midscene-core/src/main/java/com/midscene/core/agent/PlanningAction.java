package com.midscene.core.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.midscene.core.types.Rect;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanningAction {

  public String type;
  public String thought;
  public String param; // Simplified for now, should be Object or specific type
  public Rect locate; // Simplified, assumes direct Rect for now
}
