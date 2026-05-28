package com.first.components;

import com.first.UITheme;
import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class SparklinePanel extends JPanel {
    private final LinkedList<Integer> data = new LinkedList<>();
    private final int maxPoints;
    private Color lineColor;
    private int maxVal = 1;

    public SparklinePanel(int maxPoints, Color lineColor) {
        this.maxPoints = maxPoints;
        this.lineColor = lineColor;
        setOpaque(false);
        setPreferredSize(new Dimension(80, 30));
    }

    public void addValue(int value) {
        data.addLast(value);
        if (data.size() > maxPoints) data.removeFirst();
        maxVal = Math.max(1, data.stream().max(Integer::compareTo).orElse(1));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.size() < 2) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        float xStep = (float) w / (maxPoints - 1);

        // Fill area
        int[] xPoints = new int[data.size() + 2];
        int[] yPoints = new int[data.size() + 2];
        for (int i = 0; i < data.size(); i++) {
            xPoints[i] = (int)(i * xStep);
            yPoints[i] = h - (int)((float) data.get(i) / maxVal * (h - 4)) - 2;
        }
        xPoints[data.size()] = xPoints[data.size() - 1];
        yPoints[data.size()] = h;
        xPoints[data.size() + 1] = xPoints[0];
        yPoints[data.size() + 1] = h;

        g2.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 25));
        g2.fillPolygon(xPoints, yPoints, data.size() + 2);

        // Line
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < data.size() - 1; i++) {
            g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }
        g2.dispose();
    }
}
