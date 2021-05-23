package com.es.projector.net.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ShareService extends Remote {
    void updateScreenCapture(byte[] bufferedImage) throws RemoteException;

    byte[] getScreenImage() throws RemoteException;

    void addWatcher(String name) throws RemoteException;

    List<String> getWatchers() throws RemoteException;

    void clearWatchers() throws RemoteException;

    Integer getMouseX() throws RemoteException;

    Integer getMouseY() throws RemoteException;

    void setMouseX(Integer x) throws RemoteException;
    void setMouseY(Integer y) throws RemoteException;
}