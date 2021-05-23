package com.es.projector.common;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenCapture {

    public static BufferedImage captureScreen(int screenIndex) throws AWTException {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        Rectangle screenBounds = screens[screenIndex].getDefaultConfiguration().getBounds();

        Robot robot = new Robot(screens[screenIndex]);

        if(screenIndex == 0){
            return robot.createScreenCapture(screenBounds);
        }else{
            return robot.createScreenCapture(calculateScreenOffset(screens, screenBounds, screenIndex));
        }
    }

    public static Rectangle calculateScreenOffsetFromIndex(int screenIndex){
        Rectangle base = new Rectangle();
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

        return calculateScreenOffset(devices, base, screenIndex);
    }

    public static Rectangle calculateScreenOffset(GraphicsDevice[] screens, Rectangle base, int screenIndex){
        int width = 0;
        for(int i=0; i<screenIndex; i++){
            width += screens[i].getDisplayMode().getWidth();
        }

        return new Rectangle(width, 0, base.width, base.height);
    }
}
