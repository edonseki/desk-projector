package com.es.projector.net.rmi;

import com.es.projector.common.Constants;
import com.es.projector.common.ImageManipulator;
import com.es.projector.common.ScreenCapture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ContentProvider implements Runnable {

    private ShareService shareService;
    private ContentProviderListener contentProviderListener;
    private int screenIndex;

    private Thread worker;
    public static boolean running;

    public ContentProvider(ShareService shareService, int screenIndex, ContentProviderListener contentProviderListener) {
        this.shareService = shareService;
        this.contentProviderListener = contentProviderListener;
        this.screenIndex = screenIndex;
    }

    public void start(){
        this.worker = new Thread(this);
        ContentProvider.running = true;
        this.worker.start();
    }

    public void stop(){
        if(this.worker != null){
            ContentProvider.running = false;
            this.worker = null;
        }
    }

    @Override
    public void run() {
        AtomicInteger sleepTime = new AtomicInteger(0);
        while (ContentProvider.running) {
            try {
                if (sleepTime.get() == 0) {
                    shareService.clearWatchers();
                }
                List<String> watchers = shareService.getWatchers();
                if (this.contentProviderListener != null) {
                    this.contentProviderListener.onWatchersUpdated(watchers);
                }
                Point mousePoint = this.calculateMousePointer();
                shareService.setMouseX(mousePoint.x);
                shareService.setMouseY(mousePoint.y);
                final BufferedImage image = ScreenCapture.captureScreen(this.screenIndex);
                shareService.updateScreenCapture(ImageManipulator.imageToByte(image));
                if (sleepTime.addAndGet(Constants.REFRESH_RATE) >= 1000 * 5) {
                    sleepTime.set(0);
                }

                Thread.sleep(Constants.REFRESH_RATE);
            } catch (RemoteException | AWTException | InterruptedException exception) {
                // silient exception
            }
        }
    }

    private Point calculateMousePointer() {
        Point point = MouseInfo.getPointerInfo().getLocation();
        Rectangle screenRectangle = ScreenCapture.calculateScreenOffsetFromIndex(this.screenIndex);
        point.x = Math.abs(point.x - screenRectangle.x);
        return point;
    }

    public interface ContentProviderListener {
        void onWatchersUpdated(List<String> watchers);
    }
}
