package com.es.projector.net.rmi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ShareServiceImpl implements ShareService {
    private byte[] bufferedImage;
    private List<String> watchers;
    private Integer mouseX, mouseY;

    @Override
    public void updateScreenCapture(byte[] bufferedImage) throws RemoteException {
        this.bufferedImage = bufferedImage;
    }

    public byte[] getScreenImage() throws RemoteException {
        return this.bufferedImage;
    }

    public void addWatcher(String name) throws RemoteException{
        if(!this.watchers.contains(name)) {
            this.watchers.add(name);
        }
    }

    public List<String> getWatchers() throws RemoteException{
        if(watchers == null){
            watchers = new ArrayList<>();
        }
        return watchers;
    }

    public void clearWatchers() throws RemoteException{
        this.watchers = new ArrayList<>();
    }

    @Override
    public Integer getMouseX() throws RemoteException {
        return this.mouseX;
    }

    @Override
    public Integer getMouseY() throws RemoteException {
        return this.mouseY;
    }

    @Override
    public void setMouseX(Integer x) throws RemoteException {
        this.mouseX = x;
    }

    @Override
    public void setMouseY(Integer y) throws RemoteException {
        this.mouseY = y;
    }
}
