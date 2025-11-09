package com.first;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import java.util.List;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatLightLaf;

public class PacketDetails extends javax.swing.JFrame {

    private PcapHandle ha = null;
    private List<Packet> p1 = new ArrayList<>();
    private int i;

    public PacketDetails(List<Packet> p, int index, PcapHandle handle) {
        // Set FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
        p1 = p;
        i = index;
        ha = handle;
        initComponents();
        if (p1 != null && i >= 0 && i < p1.size()) {
            jLabel4.setText(String.valueOf(p1.get(i).getHeader()));
        } else {
            jLabel4.setText("Invalid packet index or packet list is empty");
        }
    }

    private void initComponents() {
        setTitle("Packet Details");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240)); // Light background

        JLabel headerLabel = new JLabel("PACKET DETAILS", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(new Color(50, 50, 50));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(headerLabel, BorderLayout.NORTH);

        jLabel4 = new JLabel("", SwingConstants.CENTER); // Placeholder for packet details
        jLabel4.setFont(new Font("Arial", Font.PLAIN, 14));
        jLabel4.setBorder(BorderFactory.createTitledBorder("Packet Header"));
        headerPanel.add(jLabel4, BorderLayout.SOUTH);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 15, 15)); // 2 rows, 3 columns
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton headerButton = new JButton("Get Packet Header");
        headerButton.setToolTipText("View the header of the selected packet");
        headerButton.addActionListener(evt -> {
            if (p1 == null || i < 0 || i >= p1.size()) {
                JOptionPane.showMessageDialog(this, "Invalid packet index.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Header(p1, i, ha).setVisible(true);
            this.setVisible(false);
        });

        JButton rawDataButton = new JButton("Get Packet Raw Data");
        rawDataButton.setToolTipText("View the raw data of the selected packet");
        rawDataButton.addActionListener(evt -> {
            if (p1 == null || i < 0 || i >= p1.size()) {
                JOptionPane.showMessageDialog(this, "Invalid packet index.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new RawData(p1, i, ha).setVisible(true);
            this.setVisible(false);
        });

        JButton payloadButton = new JButton("Get Packet Payload");
        payloadButton.setToolTipText("View the payload of the selected packet");
        payloadButton.addActionListener(evt -> {
            if (p1 == null || i < 0 || i >= p1.size()) {
                JOptionPane.showMessageDialog(this, "Invalid packet index.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Payload(p1, i, ha).setVisible(true);
            this.setVisible(false);
        });

        JButton lengthButton = new JButton("Get Packet Length");
        lengthButton.setToolTipText("View the length of the selected packet");
        lengthButton.addActionListener(evt -> {
            if (p1 == null || i < 0 || i >= p1.size()) {
                JOptionPane.showMessageDialog(this, "Invalid packet index.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Length(p1, i, ha).setVisible(true);
            this.setVisible(false);
        });

        JButton statsButton = new JButton("Check Statistics");
        statsButton.setToolTipText("View packet statistics");
        statsButton.addActionListener(evt -> {
            new CheckStats(p1, i, ha).setVisible(true);
            this.setVisible(false);
        });

        JButton dumpButton = new JButton("Dump Packets");
        dumpButton.setToolTipText("Dump packets to a file");
        dumpButton.addActionListener(evt -> {
            if (ha == null) {
                JOptionPane.showMessageDialog(this, "Capture handle not available. Start capturing first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("capture.pcap"));
            int result = chooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            java.io.File target = chooser.getSelectedFile();
            if (p1 == null || p1.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No packets to dump.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                PcapDumper dumper = ha.dumpOpen(target.getAbsolutePath());
                // Use current timestamp for all packets (since we don't store individual timestamps)
                java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
                for (Packet packet : p1) {
                    dumper.dump(packet, timestamp);
                }
                dumper.close();
                JOptionPane.showMessageDialog(this, "Dumped " + p1.size() + " packets to: " + target.getAbsolutePath(), "Success!", JOptionPane.INFORMATION_MESSAGE);
            } catch (PcapNativeException | NotOpenException e) {
                JOptionPane.showMessageDialog(this, "Dump failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(headerButton);
        buttonPanel.add(rawDataButton);
        buttonPanel.add(payloadButton);
        buttonPanel.add(lengthButton);
        buttonPanel.add(statsButton);
        buttonPanel.add(dumpButton);

        // Footer Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton exitButton = new JButton("Exit");
        exitButton.setToolTipText("Exit the application");
        exitButton.addActionListener(evt -> System.exit(0));
        footerPanel.add(exitButton);

        // Add Panels to Frame
        add(headerPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    private JLabel jLabel4;

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            // For testing, instantiate with empty packet list
            new PacketDetails(new ArrayList<>(), 0, null).setVisible(true);
        });
    }
}
