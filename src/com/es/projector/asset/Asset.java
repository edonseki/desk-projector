package com.es.projector.asset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Asset {
    public static BufferedImage getCursor() {
        InputStream is = Asset.class.getClassLoader().getResourceAsStream("com/es/projector/images/clicker.png");
        try {
            return is == null ? null : ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
