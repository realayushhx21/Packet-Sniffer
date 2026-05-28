package com.first;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import java.util.List;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PacketDetails extends javax.swing.JFrame {

    private PcapHandle ha = null;
    private List<Packet> p1 = new ArrayList<>();
    private int i;

    public PacketDetails(List<Packet> p, int index, PcapHandle handle) {
        UITheme.initTheme();
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
        setSize(900, 600);
        getContentPane().setBackground(UITheme.BG_PRIMARY);
        setLayout(new BorderLayout());

        // ─── Gradient Header ───
        jLabel4 = new JLabel("", SwingConstants.CENTER);
        jLabel4.setFont(UITheme.FONT_MONO);
        jLabel4.setForeground(UITheme.TEXT_SECONDARY);
        JPanel headerPanel = UITheme.createGradientHeader("📋  PACKET DETAILS", jLabel4);

        // ─── Button Grid — 3×3 with styled card buttons ───
        JPanel buttonPanel = new JPanel(new GridLayout(3, 3, 16, 16));
        buttonPanel.setBackground(UITheme.BG_PRIMARY);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        JButton headerButton = UITheme.createStyledButton("📋  Packet Header", UITheme.ACCENT);
        headerButton.setToolTipText("View the header of the selected packet");
        headerButton.setPreferredSize(new Dimension(220, 52));
        headerButton.addActionListener(evt -> {
            if (p1 == null || i < 0 || i >= p1.size()) {
                JOptionPane.showMessageDialog(this, "Invalid packet index.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Header(p1, i, ha).setVisible(true);
            this.setVisible(false);
        });

        JButton rawDataButton = UITheme.createStyledButton("🔢  Raw Data", UITheme.ACCENT);
        rawDataButton.setToolTipText("View the raw data of the selected packet");
        rawDataButton.setPreferredSize(new Dimension(220, 52));
        rawDataButton.addActionListener(evt -> {
            if (p1 == null || i < 0 || i >= p1.size()) {
                JOptionPane.showMessageDialog(this, "Invalid packet index.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new RawData(p1, i, ha).setVisible(true);
            this.setVisible(false);
        });

        JButton payloadButton = UITheme.createStyledButton("📦  Payload", UITheme.ACCENT);
        payloadButton.setToolTipText("View the payload of the selected packet");
        payloadButton.setPreferredSize(new Dimension(220, 52));
        payloadButton.addActionListener(evt -> {
            if (p1 == null || i < 0 || i >= p1.size()) {
                JOptionPane.showMessageDialog(this, "Invalid packet index.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Payload(p1, i, ha).setVisible(true);
            this.setVisible(false);
        });

        JButton lengthButton = UITheme.createStyledButton("📏  Packet Length", UITheme.ACCENT);
        lengthButton.setToolTipText("View the length of the selected packet");
        lengthButton.setPreferredSize(new Dimension(220, 52));
        lengthButton.addActionListener(evt -> {
            if (p1 == null || i < 0 || i >= p1.size()) {
                JOptionPane.showMessageDialog(this, "Invalid packet index.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Length(p1, i, ha).setVisible(true);
            this.setVisible(false);
        });

        JButton statsButton = UITheme.createStyledButton("📊  Statistics", UITheme.ACCENT);
        statsButton.setToolTipText("View packet statistics");
        statsButton.setPreferredSize(new Dimension(220, 52));
        statsButton.addActionListener(evt -> {
            new CheckStats(p1, i, ha).setVisible(true);
            this.setVisible(false);
        });

        JButton explainButton = UITheme.createStyledButton("🤖  Explain with AI", new Color(156, 39, 176));
        explainButton.setToolTipText("Send this packet to Gemini AI for analysis");
        explainButton.setPreferredSize(new Dimension(220, 52));
        explainButton.addActionListener(evt -> {
            if (p1 == null || i < 0 || i >= p1.size()) {
                JOptionPane.showMessageDialog(this, "Invalid packet index.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            explainWithAI();
        });

        // ─── Export Panel ───
        JPanel exportPanel = new JPanel(new BorderLayout(8, 0));
        exportPanel.setBackground(UITheme.BG_CARD);
        exportPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JComboBox<String> exportCombo = new JComboBox<>(new String[]{"PCAP", "CSV", "JSON"});
        exportCombo.setFont(UITheme.FONT_BODY);
        JButton exportButton = UITheme.createStyledButton("📤 Export", UITheme.SUCCESS);
        exportButton.setPreferredSize(new Dimension(110, 36));
        exportButton.setToolTipText("Export all captured packets in the selected format");
        exportButton.addActionListener(evt -> {
            String format = (String) exportCombo.getSelectedItem();
            exportPackets(format);
        });
        exportPanel.add(exportCombo, BorderLayout.CENTER);
        exportPanel.add(exportButton, BorderLayout.EAST);

        JPanel spacer1 = new JPanel();
        spacer1.setBackground(UITheme.BG_PRIMARY);
        JPanel spacer2 = new JPanel();
        spacer2.setBackground(UITheme.BG_PRIMARY);

        buttonPanel.add(headerButton);
        buttonPanel.add(rawDataButton);
        buttonPanel.add(payloadButton);
        buttonPanel.add(lengthButton);
        buttonPanel.add(statsButton);
        buttonPanel.add(explainButton);
        buttonPanel.add(exportPanel);
        buttonPanel.add(spacer1);
        buttonPanel.add(spacer2);

        // ─── Footer ───
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footerPanel.setBackground(UITheme.BG_PRIMARY);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(4, 20, 12, 20));

        JButton exitButton = UITheme.createStyledButton("✕  Exit", UITheme.DANGER);
        exitButton.setToolTipText("Exit the application");
        exitButton.addActionListener(evt -> System.exit(0));
        footerPanel.add(exitButton);

        add(headerPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void explainWithAI() {
        Packet packet = p1.get(i);
        String headerText = packet.getHeader() != null ? packet.getHeader().toString() : "N/A";
        String payloadText = packet.getPayload() != null ? packet.getPayload().toString() : "N/A";
        String packetData = "Header: " + headerText + "\nPayload: " + payloadText;

        JDialog loadingDialog = new JDialog(this, "Analyzing...", false);
        loadingDialog.setSize(350, 90);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.getContentPane().setBackground(UITheme.BG_SECONDARY);
        JProgressBar pb = UITheme.createStyledProgressBar("Sending to Gemini AI...");
        pb.setIndeterminate(true);
        loadingDialog.add(pb);
        loadingDialog.setVisible(true);

        new Thread(() -> {
            try {
                String explanation = GeminiExplainer.explainPacket(packetData);
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    JTextArea resultArea = UITheme.createTerminalTextArea();
                    resultArea.setText(explanation);
                    resultArea.setForeground(UITheme.TEXT_PRIMARY);
                    resultArea.setLineWrap(true);
                    resultArea.setWrapStyleWord(true);
                    JScrollPane scroll = UITheme.createStyledScrollPane(resultArea);
                    scroll.setPreferredSize(new Dimension(650, 420));
                    JOptionPane.showMessageDialog(this, scroll,
                            "🤖 AI Packet Analysis", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    JOptionPane.showMessageDialog(this,
                            "AI analysis failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "gemini-api-thread").start();
    }

    private void exportPackets(String format) {
        if (p1 == null || p1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No packets to export.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String extension;
        switch (format) {
            case "CSV": extension = "csv"; break;
            case "JSON": extension = "json"; break;
            default: extension = "pcap"; break;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("capture." + extension));
        chooser.setDialogTitle("Export as " + format);
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        File target = chooser.getSelectedFile();
        try {
            switch (format) {
                case "CSV": ExportManager.exportCSV(p1, target); break;
                case "JSON": ExportManager.exportJSON(p1, target); break;
                case "PCAP":
                    if (ha == null) {
                        JOptionPane.showMessageDialog(this, "PCAP export requires an active capture handle.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    ExportManager.exportPCAP(p1, ha, target);
                    break;
            }
            JOptionPane.showMessageDialog(this,
                    "Exported " + p1.size() + " packets to:\n" + target.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel jLabel4;

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            // For testing, instantiate with empty packet list
            new PacketDetails(new ArrayList<>(), 0, null).setVisible(true);
        });
    }
}
