package com.es.projector.ui.forms;

import com.es.projector.common.ImageManipulator;
import com.es.projector.net.rmi.ContentObserver;
import com.es.projector.net.rmi.ShareService;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class ScreenShare implements ContentObserver.ContentObserverListener {
    private JPanel mainPanel;
    private JLabel screenView;

    private ShareService shareService;
    private ContentObserver contentObserver;

    public ScreenShare(ShareService shareService) {
        this.shareService = shareService;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void observeStream() {
        this.contentObserver = new ContentObserver(this.shareService, this);
        this.contentObserver.start();
    }

    @Override
    public void onFrameUpdate(BufferedImage image) {
        BufferedImage resized = ImageManipulator.resizeWithScaling
                (image, mainPanel.getSize().width, mainPanel.getSize().height);
        screenView.setIcon(new ImageIcon(resized));
    }

    @Override
    public void onError(String error, boolean shouldClose) {
        JOptionPane.showMessageDialog(null, error);
        if(shouldClose){
            System.exit(0);
        }
    }
}
