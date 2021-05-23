package com.es.projector.net.rmi;

import com.es.projector.common.Constants;
import com.es.projector.common.ImageManipulator;
import com.es.projector.net.NetworkSession;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ContentObserver implements Runnable {

    private ShareService shareService;
    private ContentObserverListener contentObserverListener;
    private NetworkSession networkSession;

    private Thread worker;
    public static boolean running;

    public ContentObserver(ShareService shareService, ContentObserverListener contentObserverListener) {
        this.shareService = shareService;
        this.contentObserverListener = contentObserverListener;
        this.networkSession = new NetworkSession();
    }

    public void start() {
        this.worker = new Thread(this);
        ContentObserver.running = true;
        this.worker.start();
    }

    public void stop() {
        if (this.worker != null) {
            ContentObserver.running = false;
        }
    }

    @Override
    public void run() {
        while (ContentObserver.running) {
            try {
                BufferedImage image = ImageManipulator.byteToImage(
                        ContentObserver.this.shareService.getScreenImage());
                BufferedImage imageWithCursor =
                        ImageManipulator.addCursor(image, ContentObserver.this.shareService.getMouseX(), ContentObserver.this.shareService.getMouseY());
                if (this.contentObserverListener != null) {
                    this.contentObserverListener.onFrameUpdate(imageWithCursor);
                }
                this.shareService.addWatcher(this.networkSession.getHostName());
                Thread.sleep(Constants.REFRESH_RATE);
            } catch (IOException e) {
                if (this.contentObserverListener != null) {
                    this.contentObserverListener.onError(String.format("%s\n\nError:%s", e.getClass().getSimpleName(), e.getMessage()), true);
                    this.stop();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public interface ContentObserverListener {
        void onFrameUpdate(BufferedImage image);

        void onError(String error, boolean shouldClose);
    }
}
