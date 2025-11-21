package com.midscene.web.driver;

import com.microsoft.playwright.Page;
import com.midscene.core.service.PageDriver;
import com.midscene.core.types.Rect;
import com.midscene.core.types.Size;
import java.util.Base64;

public class PlaywrightDriver implements PageDriver {

  private final Page page;

  public PlaywrightDriver(Page page) {
    this.page = page;
  }

  @Override
  public String getUrl() {
    return page.url();
  }

  @Override
  public Size getViewportSize() {
    com.microsoft.playwright.options.ViewportSize size = page.viewportSize();
    return new Size(size.width, size.height);
  }

  @Override
  public String getScreenshotBase64() {
    byte[] screenshot = page.screenshot();
    return Base64.getEncoder().encodeToString(screenshot);
  }

  @Override
  public void click(Rect rect) {
    page.mouse().click(rect.left() + rect.width() / 2, rect.top() + rect.height() / 2);
  }

  @Override
  public void type(Rect rect, String text) {
    page.mouse().click(rect.left() + rect.width() / 2, rect.top() + rect.height() / 2);
    page.keyboard().type(text);
  }

  @Override
  public void scroll(Rect rect, int dx, int dy) {
    page.mouse().wheel(dx, dy);
  }

  @Override
  public void hover(Rect rect) {
    page.mouse().move(rect.left() + rect.width() / 2, rect.top() + rect.height() / 2);
  }
}
