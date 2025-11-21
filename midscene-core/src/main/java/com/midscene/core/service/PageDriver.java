package com.midscene.core.service;

import com.midscene.core.types.Rect;
import com.midscene.core.types.Size;

public interface PageDriver {

  String getUrl();

  Size getViewportSize();

  String getScreenshotBase64();

  void click(Rect rect);

  void type(Rect rect, String text);

  void scroll(Rect rect, int dx, int dy);

  void hover(Rect rect);
}
