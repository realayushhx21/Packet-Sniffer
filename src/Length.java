package com.first;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import java.util.List;
import java.util.*;
import javax.swing.*;
import java.awt.*;

public class Length extends javax.swing.JFrame {

    private PcapHandle ha = null;
    private List<Packet> p1 = new ArrayList<>();
    private int i;

    public Length(List<Packet> p, int index, PcapHandle handle) {
        UITheme.initTheme();
        p1 = p;
        i = index;
        ha = handle;
        initComponents();
        if (p != null && index >= 0 && index < p.size()) {
            jLabel4.setText(String.valueOf(p.get(index).length()));
            jLabelUnit.setText("bytes");
        } else {
            jLabel4.setText("N/A");
            jLabelUnit.setText("Invalid packet index or packet list is empty");
        }
    }

    private void initComponents() {
        setTitle("Packet Length");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(480, 380);
        getContentPane().setBackground(UITheme.BG_PRIMARY);
        setLayout(new BorderLayout());

        // Gradient Header
        JPanel headerPanel = UITheme.createGradientHeader("📏  PACKET LENGTH");

        // Large centered value display
        JPanel valuePanel = new JPanel(new GridBagLayout());
        valuePanel.setBackground(UITheme.BG_SECONDARY);
        valuePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 40, 10, 40),
                UITheme.createSectionBorder("Length")));

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBackground(UITheme.BG_SECONDARY);

        jLabel4 = new JLabel("", SwingConstants.CENTER);
        jLabel4.setFont(UITheme.FONT_BIG_VALUE);
        jLabel4.setForeground(UITheme.ACCENT_LIGHT);
        jLabel4.setAlignmentX(Component.CENTER_ALIGNMENT);

        jLabelUnit = new JLabel("", SwingConstants.CENTER);
        jLabelUnit.setFont(UITheme.FONT_SUBTITLE);
        jLabelUnit.setForeground(UITheme.TEXT_MUTED);
        jLabelUnit.setAlignmentX(Component.CENTER_ALIGNMENT);

        innerPanel.add(Box.createVerticalGlue());
        innerPanel.add(jLabel4);
        innerPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        innerPanel.add(jLabelUnit);
        innerPanel.add(Box.createVerticalGlue());

        valuePanel.add(innerPanel);

        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        footerPanel.setBackground(UITheme.BG_PRIMARY);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 14, 10));

        JButton backButton = UITheme.createStyledButton("←  Back", UITheme.ACCENT);
        backButton.setToolTipText("Return to Packet Details");
        backButton.addActionListener(evt -> {
            new PacketDetails(p1, i, ha).setVisible(true);
            this.setVisible(false);
        });
        footerPanel.add(backButton);

        add(headerPanel, BorderLayout.NORTH);
        add(valuePanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JLabel jLabel4;
    private JLabel jLabelUnit;

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new Length(new ArrayList<>(), 0, null).setVisible(true);
        });
    }
}
