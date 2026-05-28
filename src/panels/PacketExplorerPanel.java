package com.first.panels;

import com.first.*;
import com.first.components.*;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.packet.Packet;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class PacketExplorerPanel extends JPanel {
    private final List<Packet> packets;
    private PcapHandle handle;
    private final JTabbedPane tabs;
    private final JTextArea headerArea, payloadArea, rawArea;
    private final JLabel lengthValue, lengthUnit;
    private final JList<String> statsList;
    private final HexViewer hexViewer;
    private final JLabel selectedLabel;
    private int selectedIndex = -1;

    public PacketExplorerPanel(List<Packet> packets) {
        this.packets = packets;
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = new JLabel("📋 Packet Explorer");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.ACCENT);
        titleRow.add(title, BorderLayout.WEST);
        selectedLabel = new JLabel("No packet selected");
        selectedLabel.setFont(UITheme.FONT_BODY);
        selectedLabel.setForeground(UITheme.TEXT_MUTED);
        titleRow.add(selectedLabel, BorderLayout.EAST);
        add(titleRow, BorderLayout.NORTH);

        // Tabbed inspector
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.setBackground(UITheme.BG_SECONDARY);
        tabs.setForeground(UITheme.TEXT_PRIMARY);

        // Header tab
        headerArea = UITheme.createTerminalTextArea();
        headerArea.setForeground(UITheme.TEXT_PRIMARY);
        tabs.addTab("📋 Header", UITheme.createStyledScrollPane(headerArea));

        // Payload tab
        payloadArea = UITheme.createTerminalTextArea();
        tabs.addTab("📦 Payload", UITheme.createStyledScrollPane(payloadArea));

        // Raw Data tab
        rawArea = UITheme.createTerminalTextArea();
        tabs.addTab("🔢 Raw Data", UITheme.createStyledScrollPane(rawArea));

        // Hex Viewer tab
        hexViewer = new HexViewer();
        tabs.addTab("⬡ Hex View", hexViewer);

        // Length tab
        JPanel lengthPanel = new JPanel(new GridBagLayout());
        lengthPanel.setBackground(UITheme.BG_SECONDARY);
        JPanel innerLen = new JPanel();
        innerLen.setBackground(UITheme.BG_SECONDARY);
        innerLen.setLayout(new BoxLayout(innerLen, BoxLayout.Y_AXIS));
        lengthValue = new JLabel("—");
        lengthValue.setFont(UITheme.FONT_BIG_VALUE);
        lengthValue.setForeground(UITheme.ACCENT_LIGHT);
        lengthValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        lengthUnit = new JLabel("bytes");
        lengthUnit.setFont(UITheme.FONT_SUBTITLE);
        lengthUnit.setForeground(UITheme.TEXT_MUTED);
        lengthUnit.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerLen.add(Box.createVerticalGlue());
        innerLen.add(lengthValue);
        innerLen.add(Box.createRigidArea(new Dimension(0, 6)));
        innerLen.add(lengthUnit);
        innerLen.add(Box.createVerticalGlue());
        lengthPanel.add(innerLen);
        tabs.addTab("📏 Length", lengthPanel);

        // Stats tab
        statsList = UITheme.createStyledList(new DefaultListModel<>());
        statsList.setFont(UITheme.FONT_SUBTITLE);
        statsList.setFixedCellHeight(44);
        tabs.addTab("📊 Statistics", UITheme.createStyledScrollPane(statsList));

        GlassCard tabCard = new GlassCard(UITheme.ACCENT);
        tabCard.setLayout(new BorderLayout());
        tabCard.add(tabs, BorderLayout.CENTER);
        add(tabCard, BorderLayout.CENTER);

        showEmptyState();
    }

    public void setHandle(PcapHandle h) { this.handle = h; }

    public void inspectPacket(int index) {
        if (index < 0 || index >= packets.size()) return;
        selectedIndex = index;
        Packet pkt = packets.get(index);
        selectedLabel.setText("Packet #" + (index + 1) + " — " + pkt.length() + " bytes");
        selectedLabel.setForeground(UITheme.ACCENT);

        // Header
        headerArea.setText(pkt.getHeader() != null ? pkt.getHeader().toString() : "No header available");
        headerArea.setCaretPosition(0);

        // Payload
        Packet payload = pkt.getPayload();
        payloadArea.setText(payload != null ? payload.toString() : "No payload available");
        payloadArea.setCaretPosition(0);

        // Raw Data
        byte[] raw = pkt.getRawData();
        rawArea.setText(raw != null ? Arrays.toString(raw) : "No raw data available");
        rawArea.setCaretPosition(0);

        // Hex
        hexViewer.setData(raw);

        // Length
        lengthValue.setText(String.valueOf(pkt.length()));
        lengthUnit.setText("bytes");

        // Stats
        updateStats();
    }

    private void updateStats() {
        DefaultListModel<String> model = new DefaultListModel<>();
        if (handle != null && handle.isOpen()) {
            try {
                org.pcap4j.core.PcapStat stat = handle.getStats();
                model.addElement("  📥  Packets Received:  " + stat.getNumPacketsReceived());
                model.addElement("  📉  Packets Dropped:  " + stat.getNumPacketsDropped());
                model.addElement("  ⚠  Dropped by Interface:  " + stat.getNumPacketsDroppedByIf());
            } catch (Exception e) {
                model.addElement("  Statistics unavailable: " + e.getMessage());
            }
        } else {
            model.addElement("  Statistics not available: Capture handle is null or closed");
        }
        model.addElement("  📊  Total captured:  " + packets.size());
        statsList.setModel(model);
    }

    private void showEmptyState() {
        headerArea.setText("Select a packet from the Live Capture tab to inspect its contents.");
        payloadArea.setText("No packet selected.");
        rawArea.setText("No packet selected.");
        lengthValue.setText("—");
    }
}
