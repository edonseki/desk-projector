package com.es.projector.ui.dialogs;

import java.awt.image.BufferedImage;

public class Screen {
    private int index;
    private String name;
    private BufferedImage screenCapture;

    public Screen(int index, String name, BufferedImage screenCapture) {
        this.index = index;
        this.name = name;
        this.screenCapture = screenCapture;
    }

    public BufferedImage getScreenCapture() {
        return screenCapture;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
}
