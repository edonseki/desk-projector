package com.es.projector.common;

import com.es.projector.net.rmi.ContentObserver;
import com.es.projector.net.rmi.ContentProvider;

import java.awt.event.WindowEvent;

public class WindowListener implements java.awt.event.WindowListener {
    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {
        ContentProvider.running = false;
        ContentObserver.running = false;
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
