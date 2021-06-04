package com.es.projector.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

public class ScreenListDialog {
    private JList list;
    private JLabel label;
    private JOptionPane optionPane;
    private JButton okButton, cancelButton;
    private ActionListener okEvent, cancelEvent;
    private JDialog dialog;
    private List<com.es.projector.ui.dialogs.Screen> screenList;

    public ScreenListDialog(String message, List<com.es.projector.ui.dialogs.Screen> screenList) {
        List<String> names = screenList.stream().map(com.es.projector.ui.dialogs.Screen::getName).collect(Collectors.toList());
        List<BufferedImage> images = screenList.stream().map(com.es.projector.ui.dialogs.Screen::getScreenCapture).collect(Collectors.toList());
        list = new JList(names.toArray());
        list.setCellRenderer(new ImageListRenderer(images));
        label = new JLabel(message);
        this.screenList = screenList;
        createAndDisplayOptionPane();
    }

    public ScreenListDialog(String title, String message, List<com.es.projector.ui.dialogs.Screen> screenList) {
        this(message, screenList);
        dialog.setTitle(title);
    }

    private void createAndDisplayOptionPane() {
        setupButtons();
        JPanel pane = layoutComponents();
        optionPane = new JOptionPane(pane);
        optionPane.setOptions(new Object[]{okButton, cancelButton});
        dialog = optionPane.createDialog("Select Screen:");
    }

    private void setupButtons() {
        okButton = new JButton("Ok");
        okButton.addActionListener(e -> handleOkButtonClick(e));

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> handleCancelButtonClick(e));
    }

    private JPanel layoutComponents() {
        centerListElements();
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(label, BorderLayout.NORTH);
        panel.add(list, BorderLayout.CENTER);
        return panel;
    }

    private void centerListElements() {
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) list.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void setOnOk(ActionListener event) {
        okEvent = event;
    }

    public void setOnClose(ActionListener event) {
        cancelEvent = event;
    }

    private void handleOkButtonClick(ActionEvent e) {
        if (okEvent != null) {
            okEvent.actionPerformed(e);
        }
        hide();
    }

    private void handleCancelButtonClick(ActionEvent e) {
        if (cancelEvent != null) {
            cancelEvent.actionPerformed(e);
        }
        hide();
    }

    public void show() {
        dialog.setVisible(true);
    }

    private void hide() {
        dialog.setVisible(false);
    }

    public Screen getSelectedItem() {
        int index = list.getSelectedIndex();
        return this.screenList.get(index);
    }
}
