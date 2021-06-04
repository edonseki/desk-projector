package com.es.projector.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ImageListRenderer extends DefaultListCellRenderer {

    Font font = new Font("helvitica", Font.BOLD, 24);
    private List<BufferedImage> images;

    public ImageListRenderer(List<BufferedImage> images){
        this.images = images;
    }

    @Override
    public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        label.setIcon(new ImageIcon(this.images.get(index)));
        label.setHorizontalTextPosition(JLabel.RIGHT);
        label.setFont(font);
        return label;
    }
}