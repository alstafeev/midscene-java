package com.midscene.core.model;

public enum AIActionType {
  ASSERT(0),
  INSPECT_ELEMENT(1),
  EXTRACT_DATA(2),
  PLAN(3),
  DESCRIBE_ELEMENT(4),
  TEXT(5);

  private final int value;

  AIActionType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
