package com.midscene.core.types;

public record Rect(double left, double top, double width, double height, Double zoom) {

  public Rect(double left, double top, double width, double height) {
    this(left, top, width, height, null);
  }
}
