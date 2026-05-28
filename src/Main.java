package com.first.src;

import com.first.UITheme;
import com.first.SplashScreen;
import com.first.DashboardFrame;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UITheme.initTheme();
            SplashScreen splash = new SplashScreen();
            splash.showSplash(() -> {
                SwingUtilities.invokeLater(() -> {
                    DashboardFrame frame = new DashboardFrame();
                    frame.setVisible(true);
                });
            });
        });
    }
}
