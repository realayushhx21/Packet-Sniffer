package com.first.components;

import com.first.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NotificationManager {
    private static final ConcurrentLinkedQueue<NotificationPanel> activeNotifications = new ConcurrentLinkedQueue<>();
    private static JLayeredPane layeredPane;

    public static void init(JLayeredPane pane) { layeredPane = pane; }

    public static void show(String message, NotificationType type) {
        if (layeredPane == null) return;
        SwingUtilities.invokeLater(() -> {
            NotificationPanel notif = new NotificationPanel(message, type);
            activeNotifications.add(notif);
            repositionAll();
            layeredPane.add(notif, JLayeredPane.POPUP_LAYER);
            notif.animateIn();

            Timer dismiss = new Timer(4000, e -> {
                notif.animateOut(() -> {
                    layeredPane.remove(notif);
                    activeNotifications.remove(notif);
                    repositionAll();
                    layeredPane.repaint();
                });
            });
            dismiss.setRepeats(false);
            dismiss.start();
        });
    }

    private static void repositionAll() {
        if (layeredPane == null) return;
        int y = 10;
        int x = layeredPane.getWidth() - 340;
        for (NotificationPanel n : activeNotifications) {
            n.setBounds(x, y, 320, 60);
            y += 68;
        }
    }

    public enum NotificationType { INFO, WARNING, CRITICAL, SUCCESS }

    private static class NotificationPanel extends JPanel {
        private float alpha = 0f;
        private final String message;
        private final NotificationType type;

        NotificationPanel(String msg, NotificationType type) {
            this.message = msg;
            this.type = type;
            setOpaque(false);
            setPreferredSize(new Dimension(320, 60));
        }

        void animateIn() {
            Timer t = new Timer(16, e -> {
                alpha = Math.min(1f, alpha + 0.1f);
                repaint();
                if (alpha >= 1f) ((Timer) e.getSource()).stop();
            });
            t.start();
        }

        void animateOut(Runnable onDone) {
            Timer t = new Timer(16, e -> {
                alpha = Math.max(0f, alpha - 0.1f);
                repaint();
                if (alpha <= 0f) { ((Timer) e.getSource()).stop(); onDone.run(); }
            });
            t.start();
        }

        private Color getAccent() {
            switch (type) {
                case CRITICAL: return UITheme.DANGER;
                case WARNING: return UITheme.WARNING;
                case SUCCESS: return UITheme.SUCCESS;
                default: return UITheme.ACCENT;
            }
        }

        private String getIcon() {
            switch (type) {
                case CRITICAL: return "🚨";
                case WARNING: return "⚠";
                case SUCCESS: return "✓";
                default: return "ℹ";
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            int w = getWidth(), h = getHeight();
            Color accent = getAccent();

            g2.setColor(new Color(17, 24, 39, 230));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));

            g2.setColor(accent);
            g2.fill(new RoundRectangle2D.Float(0, 0, 4, h, 2, 2));

            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
            g2.setStroke(new BasicStroke(1));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 10, 10));

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2.setColor(accent);
            g2.drawString(getIcon(), 14, 36);

            g2.setFont(UITheme.FONT_BODY);
            g2.setColor(UITheme.TEXT_PRIMARY);
            String display = message.length() > 45 ? message.substring(0, 42) + "..." : message;
            g2.drawString(display, 40, 28);

            g2.setFont(UITheme.FONT_TINY);
            g2.setColor(UITheme.TEXT_MUTED);
            g2.drawString(type.name(), 40, 44);

            g2.dispose();
        }
    }
}
