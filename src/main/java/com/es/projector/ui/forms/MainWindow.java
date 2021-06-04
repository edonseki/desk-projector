package com.es.projector.ui.forms;

import com.es.projector.asset.Asset;
import com.es.projector.common.Constants;
import com.es.projector.common.ImageManipulator;
import com.es.projector.common.ScreenCapture;
import com.es.projector.common.WindowListener;
import com.es.projector.net.Client;
import com.es.projector.net.NetworkSession;
import com.es.projector.net.Server;
import com.es.projector.net.rmi.ContentProvider;
import com.es.projector.net.rmi.ShareService;
import com.es.projector.ui.dialogs.Screen;
import com.es.projector.ui.dialogs.ScreenListDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class MainWindow implements ContentProvider.ContentProviderListener, ActionListener {
    private JPanel mainPanel;
    private JLabel projectorKey;
    private JButton startSharing;
    private JTextField otherProjectorKey;
    private JButton joinButton;
    private JLabel detailsLabel;
    private JButton stopSharing;
    private JList watcherList;


    private NetworkSession networkSession;
    private Server server;
    private Client client;
    private ContentProvider contentProvider;

    public MainWindow() {
        this.networkSession = NetworkSession.instance();
        this.server = Server.init();
        this.client = Client.init();
        this.init();
    }

    private void init() {
        detailsLabel.setText(String.format("%s, Author: %s", Constants.Application.VERSION, Constants.Application.AUTHOR));

        this.initSession();

        startSharing.addActionListener(this);
        joinButton.addActionListener(this);
        stopSharing.addActionListener(this);
    }

    private void initSession() {
        networkSession.initSession();
        projectorKey.setText(networkSession.getSessionId());
    }

    private void displayWatchers(List<String> watchers) {
        if (this.hasDataChanges(watchers, watcherList.getModel())) {
            DefaultListModel listModel = new DefaultListModel();
            watchers.forEach(listModel::addElement);
            watcherList.setModel(listModel);
        }
    }

    private boolean hasDataChanges(List<String> watchers, ListModel model) {
        if (model == null ||
                (model.getSize() == 0 && watchers.size() != model.getSize()) ||
                model.getSize() > watchers.size()) {
            return true;
        }
        List<String> items = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            items.add((String) model.getElementAt(i));
        }

        for (String watcher : watchers) {
            if (!items.contains(watcher)) {
                return true;
            }
        }

        return false;
    }

    private void openScreenShareWindow(ShareService shareService) {
        JFrame jFrame = new JFrame(String.format("%s v%s", Constants.Application.NAME, Constants.Application.VERSION));
        jFrame.setContentPane(new ScreenShare(shareService).getMainPanel());
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.addWindowListener(new WindowListener());
        jFrame.setIconImage(new ImageIcon("").getImage());
        jFrame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setSize(screenSize.width, screenSize.height);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    @Override
    public void onWatchersUpdated(List<String> watchers) {
        this.displayWatchers(watchers);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!(e.getSource() instanceof JButton)) {
            return;
        }
        JButton button = (JButton) e.getSource();
        if (button.equals(this.startSharing)) {
            this.handleStartScreenShare();
        } else if (button.equals(this.stopSharing)) {
            this.stopScreenSharing();
        } else if (button.equals(this.joinButton)) {
            this.joinScreenSharing();
        }
    }

    private void handleStartScreenShare() {
        GraphicsDevice[] graphicsDevices = this.getGraphicDevices();
        if (graphicsDevices.length > 1) {
            List<Screen> screens = this.prepareScreenList(graphicsDevices);
            ScreenListDialog screenListDialog = new ScreenListDialog("Choose Screen you want to share:", screens);
            screenListDialog.setOnOk(e1 -> {
                Screen screen = screenListDialog.getSelectedItem();
                this.startScreenSharing(screen.getIndex());
            });
            screenListDialog.show();
        } else {
            this.startScreenSharing(0);
        }
    }

    private List<Screen> prepareScreenList(GraphicsDevice[] graphicsDevices) {
        List<Screen> screenList = new ArrayList<>();
        for (int i = 0; i < graphicsDevices.length; i++) {
            try {
                BufferedImage screenshot = ScreenCapture.captureScreen(i);
                screenshot = ImageManipulator.resize(screenshot, 180, 110);
                screenList.add(new Screen(i, graphicsDevices[i].getIDstring(), screenshot));
            } catch (AWTException e) {
                // sillient exception
            }
        }
        return screenList;
    }

    private void startScreenSharing(int screenIndex) {
        try {
            ShareService shareService = server.start(projectorKey.getText());
            contentProvider = new ContentProvider(shareService, screenIndex, MainWindow.this);
            contentProvider.start();
            startSharing.setEnabled(false);
            joinButton.setEnabled(false);
            stopSharing.setEnabled(true);
        } catch (RemoteException remoteException) {
            JOptionPane.showMessageDialog(null, remoteException.getMessage());
        }
    }

    private void stopScreenSharing() {
        this.contentProvider.stop();
        this.contentProvider = null;
        this.server.stop();
        stopSharing.setEnabled(false);
        joinButton.setEnabled(true);
        startSharing.setEnabled(true);
        this.onWatchersUpdated(new ArrayList<>());
        this.initSession();
    }

    private void joinScreenSharing() {
        try {
            ShareService shareService = client.connect(otherProjectorKey.getText());
            openScreenShareWindow(shareService);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage());
        }
    }

    private GraphicsDevice[] getGraphicDevices() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.getScreenDevices();
    }
}
