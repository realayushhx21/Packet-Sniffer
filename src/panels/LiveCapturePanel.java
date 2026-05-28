package com.first.panels;

import com.first.*;
import com.first.components.*;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LiveCapturePanel extends JPanel {
    private final DefaultTableModel tableModel;
    private final JTable packetTable;
    private final JComboBox<String> interfaceCombo;
    private final JTextField filterField;
    private final JButton startBtn;
    private final JLabel statusLabel;

    private List<PcapNetworkInterface> devices;
    private PcapHandle handle;
    private final List<Packet> packets;
    private final AtomicInteger packetsThisSecond;
    private final Map<String, AtomicInteger> protocolCounts;
    private final ThreatDetector threatDetector;
    private final DatabaseManager dbManager;
    private final String sessionId;
    private boolean capturing = false;

    private Runnable onPacketCallback;
    private Runnable onCaptureStarted;

    public LiveCapturePanel(List<Packet> packets, AtomicInteger packetsThisSecond,
                           Map<String, AtomicInteger> protocolCounts,
                           ThreatDetector threatDetector, DatabaseManager dbManager, String sessionId) {
        this.packets = packets;
        this.packetsThisSecond = packetsThisSecond;
        this.protocolCounts = protocolCounts;
        this.threatDetector = threatDetector;
        this.dbManager = dbManager;
        this.sessionId = sessionId;

        setLayout(new BorderLayout(0, 8));
        setBackground(UITheme.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // ─── Top Controls ───
        JPanel controlsPanel = new JPanel(new BorderLayout(8, 8));
        controlsPanel.setOpaque(false);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topRow.setOpaque(false);

        JLabel ifLbl = new JLabel("Interface:");
        ifLbl.setFont(UITheme.FONT_BODY_BOLD);
        ifLbl.setForeground(UITheme.ACCENT);
        topRow.add(ifLbl);

        interfaceCombo = new JComboBox<>();
        interfaceCombo.setFont(UITheme.FONT_MONO);
        interfaceCombo.setPreferredSize(new Dimension(400, 32));
        topRow.add(interfaceCombo);

        JButton refreshBtn = UITheme.createStyledButton("↻ Refresh", UITheme.BG_TERTIARY);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadInterfaces());
        topRow.add(refreshBtn);

        startBtn = UITheme.createStyledButton("▶  Start Capture", UITheme.SUCCESS);
        startBtn.setPreferredSize(new Dimension(160, 32));
        startBtn.addActionListener(e -> toggleCapture());
        topRow.add(startBtn);

        statusLabel = new JLabel("  ⏹ Idle");
        statusLabel.setFont(UITheme.FONT_BODY);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        topRow.add(statusLabel);

        // Filter row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterRow.setOpaque(false);
        JLabel filterLbl = new JLabel("🔍 BPF Filter:");
        filterLbl.setFont(UITheme.FONT_BODY_BOLD);
        filterLbl.setForeground(UITheme.ACCENT);
        filterRow.add(filterLbl);

        filterField = UITheme.createSearchField("e.g., tcp, udp, host 192.168.1.1, port 80");
        filterField.setPreferredSize(new Dimension(350, 30));
        filterRow.add(filterField);

        JButton applyFilter = UITheme.createStyledButton("Apply", UITheme.ACCENT);
        applyFilter.setPreferredSize(new Dimension(80, 30));
        applyFilter.addActionListener(e -> applyBpfFilter());
        filterRow.add(applyFilter);

        JButton clearFilter = UITheme.createStyledButton("Clear", UITheme.BG_TERTIARY);
        clearFilter.setPreferredSize(new Dimension(80, 30));
        clearFilter.addActionListener(e -> clearBpfFilter());
        filterRow.add(clearFilter);

        controlsPanel.add(topRow, BorderLayout.NORTH);
        controlsPanel.add(filterRow, BorderLayout.SOUTH);
        add(controlsPanel, BorderLayout.NORTH);

        // ─── Packet Table ───
        String[] cols = {"#", "Time", "Source", "Destination", "Protocol", "Length", "Info"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        packetTable = UITheme.createStyledTable(tableModel);
        packetTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        packetTable.getColumnModel().getColumn(0).setMaxWidth(50);
        packetTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        packetTable.getColumnModel().getColumn(4).setMaxWidth(80);
        packetTable.getColumnModel().getColumn(5).setMaxWidth(70);

        // Protocol color renderer for column 4
        packetTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                String proto = v != null ? v.toString() : "";
                Color pc = UITheme.getProtocolColor(proto);
                lbl.setForeground(pc);
                lbl.setFont(UITheme.FONT_BODY_BOLD);
                lbl.setHorizontalAlignment(CENTER);
                if (!s) lbl.setBackground(r % 2 == 0 ? UITheme.ROW_EVEN : UITheme.ROW_ODD);
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return lbl;
            }
        });

        JScrollPane scroll = UITheme.createStyledScrollPane(packetTable);
        GlassCard tableCard = new GlassCard(UITheme.ACCENT);
        tableCard.setLayout(new BorderLayout());
        JLabel tableLbl = new JLabel("  📋 Captured Packets");
        tableLbl.setFont(UITheme.FONT_BODY_BOLD);
        tableLbl.setForeground(UITheme.ACCENT);
        tableLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        tableCard.add(tableLbl, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        loadInterfaces();
    }

    private void loadInterfaces() {
        try {
            devices = Pcaps.findAllDevs();
            interfaceCombo.removeAllItems();
            for (PcapNetworkInterface d : devices) {
                interfaceCombo.addItem(d.getName() + " — " + d.getDescription());
            }
        } catch (PcapNativeException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleCapture() {
        if (capturing) {
            stopCapture();
        } else {
            startCapture();
        }
    }

    private void startCapture() {
        if (devices == null || devices.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No interfaces found. Click Refresh.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int idx = interfaceCombo.getSelectedIndex();
        if (idx < 0) return;

        capturing = true;
        startBtn.setText("⏹  Stop Capture");
        startBtn.setBackground(UITheme.DANGER);
        statusLabel.setText("  ● Capturing...");
        statusLabel.setForeground(UITheme.SUCCESS);
        if (onCaptureStarted != null) onCaptureStarted.run();

        PcapNetworkInterface device = devices.get(idx);
        new Thread(() -> {
            try {
                int snaplen = ConfigManager.getCaptureSnaplen();
                int timeout = ConfigManager.getCaptureTimeout();
                handle = device.openLive(snaplen, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, timeout);

                PacketListener listener = packet -> {
                    packets.add(packet);
                    packetsThisSecond.incrementAndGet();
                    String proto = PacketUtils.detectProtocol(packet);
                    protocolCounts.computeIfAbsent(proto, k -> new AtomicInteger(0)).incrementAndGet();
                    threatDetector.analyzePacket(packet);
                    if (dbManager != null) dbManager.savePacket(packet, sessionId);
                    String[] info = PacketUtils.extractPacketInfo(packet);
                    String time = new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(new java.util.Date());
                    SwingUtilities.invokeLater(() -> {
                        tableModel.addRow(new Object[]{packets.size(), time, info[0], info[1], info[2], packet.length(), info[3]});
                        if (onPacketCallback != null) onPacketCallback.run();
                        // Auto-scroll
                        int last = packetTable.getRowCount() - 1;
                        if (last >= 0) packetTable.scrollRectToVisible(packetTable.getCellRect(last, 0, true));
                    });
                };

                handle.loop(-1, listener);
            } catch (InterruptedException ignored) {
            } catch (PcapNativeException | NotOpenException e) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Capture Error", JOptionPane.ERROR_MESSAGE));
            } finally {
                SwingUtilities.invokeLater(() -> {
                    capturing = false;
                    startBtn.setText("▶  Start Capture");
                    startBtn.setBackground(UITheme.SUCCESS);
                    statusLabel.setText("  ⏹ Stopped");
                    statusLabel.setForeground(UITheme.TEXT_MUTED);
                });
            }
        }, "pcap-capture-thread").start();
    }

    private void stopCapture() {
        capturing = false;
        if (handle != null && handle.isOpen()) {
            try { handle.breakLoop(); } catch (NotOpenException ignored) {}
        }
    }

    private void applyBpfFilter() {
        if (handle == null || !handle.isOpen()) {
            JOptionPane.showMessageDialog(this, "Start capturing first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String expr = filterField.getText().trim();
        if (expr.isEmpty()) return;
        try {
            handle.setFilter(expr, BpfProgram.BpfCompileMode.OPTIMIZE);
            NotificationManager.show("Filter applied: " + expr, NotificationManager.NotificationType.SUCCESS);
        } catch (PcapNativeException | NotOpenException e) {
            JOptionPane.showMessageDialog(this, "Invalid BPF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearBpfFilter() {
        if (handle == null || !handle.isOpen()) return;
        try {
            handle.setFilter("", BpfProgram.BpfCompileMode.OPTIMIZE);
            filterField.setText("");
            NotificationManager.show("Filter cleared", NotificationManager.NotificationType.INFO);
        } catch (PcapNativeException | NotOpenException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public PcapHandle getHandle() { return handle; }
    public JTable getPacketTable() { return packetTable; }
    public void setOnPacketCallback(Runnable cb) { this.onPacketCallback = cb; }
    public void setOnCaptureStarted(Runnable cb) { this.onCaptureStarted = cb; }
    public boolean isCapturing() { return capturing; }
}
