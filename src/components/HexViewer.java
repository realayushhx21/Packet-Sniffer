package com.first.components;

import com.first.UITheme;
import javax.swing.*;
import java.awt.*;

public class HexViewer extends JPanel {
    private byte[] data;
    private final JTextPane hexPane;

    public HexViewer() {
        setLayout(new BorderLayout());
        setBackground(UITheme.TERMINAL_BG);
        setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_TERTIARY);
        header.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        JLabel hdrLbl = new JLabel("Offset    00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F   ASCII");
        hdrLbl.setFont(UITheme.FONT_MONO_SM);
        hdrLbl.setForeground(UITheme.ACCENT);
        header.add(hdrLbl);
        add(header, BorderLayout.NORTH);

        hexPane = new JTextPane();
        hexPane.setEditable(false);
        hexPane.setFont(UITheme.FONT_MONO);
        hexPane.setBackground(UITheme.TERMINAL_BG);
        hexPane.setForeground(UITheme.TEXT_PRIMARY);
        hexPane.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(hexPane);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.TERMINAL_BG);
        add(scroll, BorderLayout.CENTER);
    }

    public void setData(byte[] rawData) {
        this.data = rawData;
        if (rawData == null || rawData.length == 0) {
            hexPane.setText("No data available");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int offset = 0; offset < rawData.length; offset += 16) {
            sb.append(String.format("%08X  ", offset));
            StringBuilder ascii = new StringBuilder();
            for (int j = 0; j < 16; j++) {
                if (j == 8) sb.append(" ");
                if (offset + j < rawData.length) {
                    int b = rawData[offset + j] & 0xFF;
                    sb.append(String.format("%02X ", b));
                    ascii.append(b >= 32 && b < 127 ? (char) b : '.');
                } else {
                    sb.append("   ");
                    ascii.append(' ');
                }
            }
            sb.append("  ").append(ascii).append("\n");
        }
        hexPane.setText(sb.toString());
        hexPane.setCaretPosition(0);
    }
}
