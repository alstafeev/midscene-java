package com.midscene.core.utils;

import com.midscene.core.types.Size;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImageUtilsTest {

  private String createSampleImageBase64(int width, int height) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    return Base64.getEncoder().encodeToString(baos.toByteArray());
  }

  @Test
  public void testGetImageSize() throws IOException {
    String base64 = createSampleImageBase64(100, 50);
    Size size = ImageUtils.getImageSize(base64);
    Assertions.assertEquals(100, size.width());
    Assertions.assertEquals(50, size.height());
  }

  @Test
  public void testResizeImage() throws IOException {
    String base64 = createSampleImageBase64(200, 200);
    String resizedBase64 = ImageUtils.resizeImage(base64, 100, 100);

    Size newSize = ImageUtils.getImageSize(resizedBase64);
    Assertions.assertEquals(100, newSize.width());
    Assertions.assertEquals(100, newSize.height());
  }
}
