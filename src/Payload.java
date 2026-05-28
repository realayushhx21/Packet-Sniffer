package com.first;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import java.util.List;
import java.util.*;
import javax.swing.*;
import java.awt.*;

public class Payload extends javax.swing.JFrame {

    private PcapHandle ha = null;
    private List<Packet> p1 = new ArrayList<>();
    private int i;

    public Payload(List<Packet> p, int index, PcapHandle handle) {
        UITheme.initTheme();
        p1 = p;
        i = index;
        ha = handle;
        initComponents();
        if (p != null && index >= 0 && index < p.size()) {
            org.pcap4j.packet.Packet payload = p.get(index).getPayload();
            if (payload != null) {
                jTextArea1.setText(String.valueOf(payload));
            } else {
                jTextArea1.setText("No payload available for this packet");
            }
        } else {
            jTextArea1.setText("Invalid packet index or packet list is empty");
        }
    }

    private void initComponents() {
        setTitle("Packet Payload");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(700, 550);
        getContentPane().setBackground(UITheme.BG_PRIMARY);
        setLayout(new BorderLayout());

        // Gradient Header
        JPanel headerPanel = UITheme.createGradientHeader("📦  PAYLOAD DETAILS");

        // Terminal-styled Payload Display
        jTextArea1 = UITheme.createTerminalTextArea();
        JScrollPane scrollPane = UITheme.createStyledScrollPane(jTextArea1);
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UITheme.BG_PRIMARY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 8, 20));
        JPanel borderedContent = UITheme.createTitledPanel("Payload Content", scrollPane);
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

    private JTextArea jTextArea1;

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new Payload(new ArrayList<>(), 0, null).setVisible(true);
        });
    }
}
