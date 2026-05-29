package com.midscene.core.pojo.response;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExtractAiResponse<T> {

  private T data;
  private String thought;
  private List<String> errors;
}
