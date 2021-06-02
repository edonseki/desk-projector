package com.es.projector.common;

import com.es.projector.asset.Asset;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class ImageManipulator {
    public static byte[] imageToByte(final BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", baos);
        } catch (IOException e) {
            return null;
        }
        return baos.toByteArray();
    }

    public static BufferedImage byteToImage(byte[] imageData) {
        try {
            return ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static BufferedImage resize(BufferedImage img, int width, int height) {
        return getResizedBufferedImage(img, width, height);
    }

    public static BufferedImage resizeWithScaling(BufferedImage img, int width, int height) {
        double scaleX = (double) width / img.getWidth();
        double scaleY = (double) height / img.getHeight();
        double scale = Math.min(scaleX, scaleY);

        int w = (int) (img.getWidth() * scale);
        int h = (int) (img.getHeight() * scale);

        return getResizedBufferedImage(img, w, h);
    }

    private static BufferedImage getResizedBufferedImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }


    public static BufferedImage addCursor(BufferedImage image, int x, int y){
        return addImage(image, Asset.getCursor(), 1.0F, x, y);
    }

    public static BufferedImage addImage(BufferedImage originalImage, BufferedImage newImage,
                          float opaque, int x, int y) {
        Graphics2D g2d = originalImage.createGraphics();
        g2d.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaque));
        g2d.drawImage(newImage, x, y, newImage.getWidth(),newImage.getHeight(),null);
        g2d.dispose();
        return originalImage;
    }
}
