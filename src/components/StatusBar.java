package com.first.components;

import com.first.UITheme;
import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {
    private final JLabel captureStatus;
    private final JLabel packetRate;
    private final JLabel totalPackets;
    private final JLabel dbStatus;
    private final JLabel memoryUsage;

    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_TOPBAR);
        setPreferredSize(new Dimension(0, UITheme.STATUSBAR_HEIGHT));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        left.setOpaque(false);

        captureStatus = createStatusLabel("⏹ Idle", UITheme.TEXT_MUTED);
        left.add(captureStatus);
        left.add(createDivider());

        packetRate = createStatusLabel("0 pkt/s", UITheme.TEXT_SECONDARY);
        left.add(packetRate);
        left.add(createDivider());

        totalPackets = createStatusLabel("Total: 0", UITheme.TEXT_SECONDARY);
        left.add(totalPackets);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 6));
        right.setOpaque(false);

        dbStatus = createStatusLabel("● DB Connected", UITheme.SUCCESS);
        right.add(dbStatus);
        right.add(createDivider());

        memoryUsage = createStatusLabel("", UITheme.TEXT_MUTED);
        right.add(memoryUsage);
        updateMemory();

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);

        Timer memTimer = new Timer(3000, e -> updateMemory());
        memTimer.start();
    }

    private JLabel createStatusLabel(String text, Color fg) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(fg);
        return lbl;
    }

    private JSeparator createDivider() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 16));
        sep.setForeground(UITheme.BORDER_COLOR);
        return sep;
    }

    public void setCaptureActive(boolean active) {
        captureStatus.setText(active ? "● Capturing" : "⏹ Idle");
        captureStatus.setForeground(active ? UITheme.SUCCESS : UITheme.TEXT_MUTED);
    }

    public void setPacketRate(int rate) {
        packetRate.setText(rate + " pkt/s");
        packetRate.setForeground(rate > 100 ? UITheme.WARNING : UITheme.TEXT_SECONDARY);
    }

    public void setTotalPackets(int total) {
        totalPackets.setText("Total: " + String.format("%,d", total));
    }

    public void setDbStatus(boolean connected) {
        dbStatus.setText(connected ? "● DB Connected" : "● DB Offline");
        dbStatus.setForeground(connected ? UITheme.SUCCESS : UITheme.DANGER);
    }

    private void updateMemory() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long total = rt.totalMemory() / (1024 * 1024);
        memoryUsage.setText("Mem: " + used + "/" + total + " MB");
    }
}
