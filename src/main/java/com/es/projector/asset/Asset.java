package com.es.projector.asset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Asset {
    private static BufferedImage cursor;
    public static BufferedImage getCursor() {
        if(cursor != null){
            return cursor;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream("clicker.png");
        try {
            if(is != null) {
                cursor = ImageIO.read(is);
                return cursor;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
