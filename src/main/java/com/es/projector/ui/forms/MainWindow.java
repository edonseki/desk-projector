package com.es.projector.ui.forms;

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
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    }

    public void init() {
        detailsLabel.setText(String.format("%s, Author: %s", Constants.Application.VERSION, Constants.Application.AUTHOR));

        this.initSession();

        startSharing.addActionListener(this);
        joinButton.addActionListener(this);
        stopSharing.addActionListener(this);
        otherProjectorKey.addActionListener(this);
    }

    private void initSession() {
        networkSession.initSession();
        String sessionId = networkSession.getSessionId();
        if (sessionId == null) {
            JOptionPane.showMessageDialog(null, Constants.Texts.UNABLE_TO_GENERATE_SESSION);
            System.exit(0);
        }
        projectorKey.setText(sessionId);
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
        ScreenShare screenShare = new ScreenShare(shareService);
        JFrame jFrame = new JFrame(String.format("%s v%s", Constants.Application.NAME, Constants.Application.VERSION));
        jFrame.setContentPane(screenShare.getMainPanel());
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.addWindowListener(new WindowListener());
        jFrame.setIconImage(new ImageIcon("").getImage());
        jFrame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setSize(screenSize.width, screenSize.height);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        screenShare.observeStream();
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
        if (e.getSource() instanceof JButton) {
            JButton button = (JButton) e.getSource();
            if (button.equals(this.startSharing)) {
                this.handleStartScreenShare();
            } else if (button.equals(this.stopSharing)) {
                this.stopScreenSharing();
            } else if (button.equals(this.joinButton)) {
                this.joinScreenSharing();
            }
        }
        if (e.getSource() instanceof JTextField) {
            JTextField textField = (JTextField) e.getSource();
            if (textField.equals(this.otherProjectorKey)) {
                this.joinScreenSharing();
            }
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
            if (shareService == null) {
                JOptionPane.showMessageDialog(null, Constants.Texts.SERVER_NOT_STARTED);
                return;
            }
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

    public static void main(String[] args) {
        initLookAndFeel();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    SwingUtilities.invokeLater(() -> {
                        MainWindow mainWindow = new MainWindow();
                        JFrame jFrame = new JFrame(String.format("%s v%s", Constants.Application.NAME, Constants.Application.VERSION));
                        jFrame.setContentPane(mainWindow.getMainPanel());
                        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        jFrame.pack();
                        jFrame.setSize(317, 450);
                        jFrame.setLocationRelativeTo(null);
                        jFrame.setResizable(false);
                        jFrame.setVisible(true);

                        mainWindow.init();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                super.run();
            }
        }.start();
    }

    private static void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // silient exception
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(12, 4, new Insets(20, 10, 0, 10), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(2);
        label1.setText("You Projector Key");
        mainPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        projectorKey = new JLabel();
        Font projectorKeyFont = this.$$$getFont$$$(null, Font.BOLD, 24, projectorKey.getFont());
        if (projectorKeyFont != null) projectorKey.setFont(projectorKeyFont);
        projectorKey.setText("-");
        mainPanel.add(projectorKey, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startSharing = new JButton();
        startSharing.setBackground(new Color(-855310));
        startSharing.setText("Start Sharing");
        mainPanel.add(startSharing, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, -1, -1, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setHorizontalAlignment(0);
        label2.setHorizontalTextPosition(2);
        label2.setText("Join other Projector");
        mainPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 40), null, null, 0, false));
        otherProjectorKey = new JTextField();
        mainPanel.add(otherProjectorKey, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        joinButton = new JButton();
        joinButton.setText("Join");
        mainPanel.add(joinButton, new com.intellij.uiDesigner.core.GridConstraints(9, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(10, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        detailsLabel = new JLabel();
        Font detailsLabelFont = this.$$$getFont$$$(null, -1, 8, detailsLabel.getFont());
        if (detailsLabelFont != null) detailsLabel.setFont(detailsLabelFont);
        detailsLabel.setText("-");
        mainPanel.add(detailsLabel, new com.intellij.uiDesigner.core.GridConstraints(11, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        stopSharing = new JButton();
        stopSharing.setEnabled(false);
        stopSharing.setText("Stop Sharing");
        mainPanel.add(stopSharing, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        watcherList = new JList();
        mainPanel.add(watcherList, new com.intellij.uiDesigner.core.GridConstraints(5, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Watchers:");
        mainPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(4, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
