package com.first.components;

import com.first.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class MetricCard extends JPanel {
    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final SparklinePanel sparkline;
    private Color accentColor;
    private int currentValue = 0;

    public MetricCard(String icon, String title, Color accent) {
        this.accentColor = accent;
        setOpaque(false);
        setLayout(new BorderLayout(8, 4));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setPreferredSize(new Dimension(180, 110));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        iconLabel.setForeground(accent);
        topRow.add(iconLabel, BorderLayout.WEST);

        sparkline = new SparklinePanel(20, accent);
        sparkline.setPreferredSize(new Dimension(60, 24));
        topRow.add(sparkline, BorderLayout.EAST);
        add(topRow, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        valueLabel = new JLabel("0");
        valueLabel.setFont(UITheme.FONT_METRIC);
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_MUTED);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        center.add(valueLabel);
        center.add(Box.createRigidArea(new Dimension(0, 2)));
        center.add(titleLabel);
        add(center, BorderLayout.CENTER);
    }

    public void setValue(int newValue) {
        int old = currentValue;
        currentValue = newValue;
        sparkline.addValue(newValue);
        UITheme.animateCounter(valueLabel, old, newValue, 400);
    }

    public void setValueText(String text) {
        valueLabel.setText(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), arc = UITheme.CARD_ARC;

        g2.setColor(UITheme.BG_GLASS);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Top accent line
        g2.setColor(accentColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, 3, 2, 2));

        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
        g2.setStroke(new BasicStroke(1));
        g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc));
        g2.dispose();
        super.paintComponent(g);
    }
}
