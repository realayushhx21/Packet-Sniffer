package com.first.components;

import com.first.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class GlassCard extends JPanel {
    private Color borderAccent;
    private boolean glowEnabled = false;

    public GlassCard() {
        this(UITheme.BORDER_COLOR);
    }

    public GlassCard(Color accent) {
        this.borderAccent = accent;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
    }

    public void setGlow(boolean glow) { this.glowEnabled = glow; repaint(); }
    public void setBorderAccent(Color c) { this.borderAccent = c; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), arc = UITheme.CARD_ARC;

        if (glowEnabled) {
            g2.setColor(new Color(borderAccent.getRed(), borderAccent.getGreen(), borderAccent.getBlue(), 20));
            g2.fill(new RoundRectangle2D.Float(-4, -4, w + 8, h + 8, arc + 4, arc + 4));
        }

        g2.setColor(UITheme.BG_GLASS);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        GradientPaint borderGrad = new GradientPaint(0, 0, borderAccent, w, h,
                new Color(borderAccent.getRed(), borderAccent.getGreen(), borderAccent.getBlue(), 40));
        g2.setPaint(borderGrad);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));
        g2.dispose();
        super.paintComponent(g);
    }
}
