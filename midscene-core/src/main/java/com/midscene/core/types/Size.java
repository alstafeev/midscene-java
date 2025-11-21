package com.midscene.core.types;

public record Size(double width, double height, Double dpr) {

  public Size(double width, double height) {
    this(width, height, null);
  }
}
