package com.first;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import java.util.List;
import java.util.*;
import javax.swing.*;
import java.awt.*;

public class CheckStats extends javax.swing.JFrame {

    private PcapHandle ha = null;
    private List<Packet> p1 = new ArrayList<>();
    private int i;

    public CheckStats(List<Packet> p, int index, PcapHandle handle) {
        UITheme.initTheme();
        p1 = p;
        i = index;
        ha = handle;
        initComponents();
        populateStatistics();
    }

    private void populateStatistics() {
        if (ha == null) {
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("  Statistics not available: Capture handle is null");
            jList1.setModel(model);
            return;
        }
        try {
            PcapStat stat = ha.getStats();
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("  📥  Packets Received:  " + stat.getNumPacketsReceived());
            model.addElement("  📉  Packets Dropped:  " + stat.getNumPacketsDropped());
            model.addElement("  ⚠  Dropped by Interface:  " + stat.getNumPacketsDroppedByIf());
            jList1.setModel(model);
        } catch (PcapNativeException | NotOpenException e) {
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("  Error fetching statistics: " + e.getMessage());
            jList1.setModel(model);
            JOptionPane.showMessageDialog(this, "Error fetching statistics: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setTitle("Packet Statistics");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(550, 420);
        getContentPane().setBackground(UITheme.BG_PRIMARY);
        setLayout(new BorderLayout());

        // Gradient Header
        JPanel headerPanel = UITheme.createGradientHeader("📊  STATISTICS");

        // Statistics List
        jList1 = UITheme.createStyledList(new DefaultListModel<>());
        jList1.setFont(UITheme.FONT_SUBTITLE);
        jList1.setFixedCellHeight(44);
        JScrollPane scrollPane = UITheme.createStyledScrollPane(jList1);
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UITheme.BG_PRIMARY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        JPanel borderedContent = UITheme.createTitledPanel("Capture Statistics", scrollPane);
        contentPanel.add(borderedContent, BorderLayout.CENTER);

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
        add(contentPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JList<String> jList1;

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new CheckStats(new ArrayList<>(), 0, null).setVisible(true);
        });
    }
}
