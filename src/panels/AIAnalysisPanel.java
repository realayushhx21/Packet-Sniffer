package com.first.panels;

import com.first.*;
import com.first.components.*;
import org.pcap4j.packet.Packet;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class AIAnalysisPanel extends JPanel {
    private final JTextArea chatArea;
    private final JComboBox<String> packetSelector;
    private final List<Packet> packets;
    private final JButton analyzeBtn;
    private final JProgressBar loadingBar;

    public AIAnalysisPanel(List<Packet> packets) {
        this.packets = packets;
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Title
        JLabel title = new JLabel("🤖 AI Security Analyst — Powered by Gemini");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.ACCENT_PURPLE);
        add(title, BorderLayout.NORTH);

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(UITheme.FONT_MONO);
        chatArea.setBackground(UITheme.TERMINAL_BG);
        chatArea.setForeground(UITheme.TEXT_PRIMARY);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        chatArea.setText("╔══════════════════════════════════════════════════════╗\n" +
                         "║  Packet Sniffer AI Security Analyst                  ║\n" +
                         "║  Select a captured packet and click 'Analyze'        ║\n" +
                         "║  to get AI-powered security insights.                ║\n" +
                         "╚══════════════════════════════════════════════════════╝\n\n");

        GlassCard chatCard = new GlassCard(UITheme.ACCENT_PURPLE);
        chatCard.setLayout(new BorderLayout());
        chatCard.add(UITheme.createStyledScrollPane(chatArea), BorderLayout.CENTER);
        add(chatCard, BorderLayout.CENTER);

        // Bottom controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controls.setOpaque(false);

        JLabel selLbl = new JLabel("Packet #:");
        selLbl.setFont(UITheme.FONT_BODY_BOLD);
        selLbl.setForeground(UITheme.ACCENT);
        controls.add(selLbl);

        packetSelector = new JComboBox<>();
        packetSelector.setFont(UITheme.FONT_MONO);
        packetSelector.setPreferredSize(new Dimension(300, 32));
        controls.add(packetSelector);

        JButton refreshBtn = UITheme.createStyledButton("↻", UITheme.BG_TERTIARY);
        refreshBtn.setPreferredSize(new Dimension(40, 32));
        refreshBtn.setToolTipText("Refresh packet list");
        refreshBtn.addActionListener(e -> refreshPacketList());
        controls.add(refreshBtn);

        analyzeBtn = UITheme.createStyledButton("🤖  Analyze with AI", UITheme.ACCENT_PURPLE);
        analyzeBtn.setPreferredSize(new Dimension(180, 34));
        analyzeBtn.addActionListener(e -> analyzePacket());
        controls.add(analyzeBtn);

        JButton summarizeBtn = UITheme.createStyledButton("📊  Traffic Summary", UITheme.ACCENT);
        summarizeBtn.setPreferredSize(new Dimension(160, 34));
        summarizeBtn.addActionListener(e -> summarizeTraffic());
        controls.add(summarizeBtn);

        loadingBar = UITheme.createStyledProgressBar("Sending to Gemini AI...");
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setPreferredSize(new Dimension(200, 20));
        controls.add(loadingBar);

        add(controls, BorderLayout.SOUTH);
    }

    public void refreshPacketList() {
        packetSelector.removeAllItems();
        int count = Math.min(packets.size(), 500);
        for (int i = 0; i < count; i++) {
            String[] info = PacketUtils.extractPacketInfo(packets.get(i));
            packetSelector.addItem("#" + (i + 1) + " " + info[2] + " " + info[0] + " → " + info[1]);
        }
    }

    private void analyzePacket() {
        int idx = packetSelector.getSelectedIndex();
        if (idx < 0 || idx >= packets.size()) {
            appendChat("SYSTEM", "No packet selected. Refresh the list first.", UITheme.WARNING);
            return;
        }
        Packet pkt = packets.get(idx);
        String headerText = pkt.getHeader() != null ? pkt.getHeader().toString() : "N/A";
        String payloadText = pkt.getPayload() != null ? pkt.getPayload().toString() : "N/A";
        String packetData = "Header: " + headerText + "\nPayload: " + payloadText;

        appendChat("USER", "Analyze packet #" + (idx + 1), UITheme.ACCENT);
        analyzeBtn.setEnabled(false);
        loadingBar.setVisible(true);

        new Thread(() -> {
            try {
                String explanation = GeminiExplainer.explainPacket(packetData);
                SwingUtilities.invokeLater(() -> {
                    appendChat("AI ANALYST", explanation, UITheme.ACCENT_PURPLE);
                    analyzeBtn.setEnabled(true);
                    loadingBar.setVisible(false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendChat("ERROR", "Analysis failed: " + ex.getMessage(), UITheme.DANGER);
                    analyzeBtn.setEnabled(true);
                    loadingBar.setVisible(false);
                });
            }
        }, "gemini-api-thread").start();
    }

    private void summarizeTraffic() {
        if (packets.isEmpty()) {
            appendChat("SYSTEM", "No packets captured yet.", UITheme.WARNING);
            return;
        }
        StringBuilder summary = new StringBuilder();
        summary.append("Total packets: ").append(packets.size()).append("\n");
        java.util.Map<String, Integer> protoCounts = new java.util.HashMap<>();
        for (Packet p : packets) {
            String proto = PacketUtils.detectProtocol(p);
            protoCounts.merge(proto, 1, Integer::sum);
        }
        summary.append("Protocol breakdown:\n");
        protoCounts.forEach((k, v) -> summary.append("  ").append(k).append(": ").append(v).append("\n"));

        String data = "Summarize this network traffic and identify any concerns:\n" + summary;
        appendChat("USER", "Generate traffic summary", UITheme.ACCENT);
        analyzeBtn.setEnabled(false);
        loadingBar.setVisible(true);

        new Thread(() -> {
            try {
                String result = GeminiExplainer.explainPacket(data);
                SwingUtilities.invokeLater(() -> {
                    appendChat("AI ANALYST", result, UITheme.ACCENT_PURPLE);
                    analyzeBtn.setEnabled(true);
                    loadingBar.setVisible(false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendChat("ERROR", "Summary failed: " + ex.getMessage(), UITheme.DANGER);
                    analyzeBtn.setEnabled(true);
                    loadingBar.setVisible(false);
                });
            }
        }, "gemini-summary-thread").start();
    }

    private void appendChat(String role, String message, Color roleColor) {
        String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        chatArea.append("┌─ " + role + " [" + time + "]\n");
        chatArea.append("│  " + message.replace("\n", "\n│  ") + "\n");
        chatArea.append("└────────────────────────────────────────\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
