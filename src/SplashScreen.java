package com.first;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class SplashScreen extends JWindow {
    private float progress = 0f;
    private String statusText = "Initializing systems...";
    private final String[] loadSteps = {
        "Loading threat detection engine...",
        "Connecting to database...",
        "Initializing packet capture...",
        "Loading AI analysis module...",
        "Preparing dashboard..."
    };

    public SplashScreen() {
        setSize(520, 320);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Background
                g2.setColor(new Color(11, 16, 32));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));

                // Grid pattern
                g2.setColor(new Color(0, 229, 255, 8));
                g2.setStroke(new BasicStroke(0.5f));
                for (int x = 0; x < w; x += 30) g2.drawLine(x, 0, x, h);
                for (int y = 0; y < h; y += 30) g2.drawLine(0, y, w, y);

                // Border glow
                g2.setColor(new Color(0, 229, 255, 40));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 16, 16));

                // Icon
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 48));
                g2.setColor(new Color(0, 229, 255));
                g2.drawString("⚡", w / 2 - 28, 90);

                // Title
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                g2.setColor(new Color(0, 229, 255));
                g2.drawString("Packet Sniffer", w / 2 - 70, 140);

                // Subtitle
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(new Color(148, 163, 184));
                g2.drawString("Network Security Intelligence Platform", w / 2 - 130, 165);

                // Progress bar background
                int barX = 60, barY = 210, barW = w - 120, barH = 4;
                g2.setColor(new Color(30, 41, 59));
                g2.fill(new RoundRectangle2D.Float(barX, barY, barW, barH, 2, 2));

                // Progress bar fill
                GradientPaint gp = new GradientPaint(barX, barY, new Color(0, 229, 255),
                        barX + barW, barY, new Color(139, 92, 246));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(barX, barY, barW * progress, barH, 2, 2));

                // Glow on progress tip
                if (progress > 0) {
                    float tipX = barX + barW * progress;
                    g2.setColor(new Color(0, 229, 255, 60));
                    g2.fillOval((int)tipX - 6, barY - 4, 12, 12);
                }

                // Status text
                g2.setFont(new Font("Consolas", Font.PLAIN, 11));
                g2.setColor(new Color(100, 116, 139));
                g2.drawString("> " + statusText, barX, barY + 28);

                // Version
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(new Color(71, 85, 105));
                g2.drawString("v2.0.0 — Enterprise Edition", w / 2 - 80, h - 20);

                g2.dispose();
            }
        };
        content.setOpaque(false);
        setContentPane(content);
    }

    public void showSplash(Runnable onComplete) {
        setVisible(true);
        Timer timer = new Timer(40, null);
        int[] step = {0};
        timer.addActionListener(e -> {
            step[0]++;
            progress = Math.min(1f, step[0] / 50f);
            int idx = Math.min(step[0] / 10, loadSteps.length - 1);
            statusText = loadSteps[idx];
            getContentPane().repaint();
            if (step[0] >= 50) {
                timer.stop();
                Timer fadeOut = new Timer(16, null);
                float[] alpha = {1f};
                fadeOut.addActionListener(e2 -> {
                    alpha[0] -= 0.08f;
                    if (alpha[0] <= 0) {
                        ((Timer) e2.getSource()).stop();
                        dispose();
                        onComplete.run();
                    } else {
                        setOpacity(Math.max(0, alpha[0]));
                    }
                });
                fadeOut.start();
            }
        });
        timer.start();
    }
}
