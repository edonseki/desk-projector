package com.es.projector;

import com.es.projector.common.Constants;
import com.es.projector.ui.forms.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        initLookAndFeel();

        JFrame jFrame = new JFrame(String.format("%s v%s", Constants.Application.NAME, Constants.Application.VERSION));
        jFrame.setContentPane(new MainWindow().getMainPanel());
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setSize(317, 450);
        jFrame.setLocationRelativeTo(null);
        jFrame.setResizable(false);
        jFrame.setVisible(true);
    }

    private static void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // silient exception
        }
    }
}
