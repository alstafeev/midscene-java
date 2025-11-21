package com.midscene.core.utils;

import com.midscene.core.types.Size;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

public class ImageUtils {

  public static Size getImageSize(String base64) {
    try {
      byte[] bytes = Base64.getDecoder().decode(base64);
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
      return new Size(image.getWidth(), image.getHeight());
    } catch (IOException e) {
      throw new RuntimeException("Failed to read image", e);
    }
  }

  public static String resizeImage(String base64, int targetWidth, int targetHeight) {
    try {
      byte[] bytes = Base64.getDecoder().decode(base64);
      BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(bytes));

      BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = resizedImage.createGraphics();
      g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
      g.dispose();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(resizedImage, "png", baos);
      return Base64.getEncoder().encodeToString(baos.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException("Failed to resize image", e);
    }
  }
}
