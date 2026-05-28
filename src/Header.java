package com.first;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import java.util.List;
import java.util.*;
import javax.swing.*;
import java.awt.*;

public class Header extends javax.swing.JFrame {

    private PcapHandle ha = null;
    private List<Packet> p1 = new ArrayList<>();
    private int i;

    public Header(List<Packet> p, int index, PcapHandle handle) {
        UITheme.initTheme();
        p1 = p;
        i = index;
        ha = handle;
        initComponents();
        if (p != null && index >= 0 && index < p.size()) {
            jLabel4.setText(String.valueOf(p.get(index).getHeader()));
        } else {
            jLabel4.setText("Invalid packet index or packet list is empty");
        }
    }

    private void initComponents() {
        setTitle("Packet Header");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 380);
        getContentPane().setBackground(UITheme.BG_PRIMARY);
        setLayout(new BorderLayout());

        // Gradient Header
        JPanel headerPanel = UITheme.createGradientHeader("📋  PACKET HEADER");

        // Packet Header Details
        jLabel4 = new JLabel("", SwingConstants.CENTER); // Display header details dynamically
        jLabel4.setFont(UITheme.FONT_MONO_LG);
        jLabel4.setForeground(UITheme.TEXT_PRIMARY);
        JPanel contentPanel = UITheme.createTitledPanel("Header Details", jLabel4);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(16, 20, 10, 20),
                contentPanel.getBorder()));

        // Footer Panel with Back Button
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
        add(contentPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    private JLabel jLabel4;

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            // For testing, instantiate with empty packet list
            new Header(new ArrayList<>(), 0, null).setVisible(true);
        });
    }
}
