package com.midscene.core.pojo.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocateAiResponse {

  private boolean found;
  private int[] bbox;
  private int[] center;
  private String description;
  private String reason;
}
