package com.first;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.*;

public class ViewPackets extends javax.swing.JFrame {

    PcapNetworkInterface device;
    List<Packet> p = new ArrayList<>();
    int index = -1;
    PcapHandle handle = null;
    private JLabel jLabel7;
    private JLabel jLabel6;

    private XYSeries packetsSeries;
    private DefaultPieDataset protocolDataset;
    private final AtomicInteger packetsThisSecond = new AtomicInteger(0);
    private final Map<String, AtomicInteger> protocolCounts = new ConcurrentHashMap<>();
    private int chartTimeCounter = 0;

    private JTextField filterField;
    private ThreatDetector threatDetector;
    private JTextArea alertsArea;
    private DatabaseManager dbManager;
    private String sessionId;

    private File pcapFile;
    private int replayMode;
    private boolean isFileMode = false;

    public ViewPackets(List<PcapNetworkInterface> d, int i) {
        UITheme.initTheme();
        initComponents();
        if (d != null && i >= 0 && i < d.size()) {
            jLabel7.setText("  Interface: " + d.get(i).getDescription());
            index = i;
            device = d.get(i);
        } else {
            jLabel7.setText("Invalid interface selection");
            JOptionPane.showMessageDialog(this, "Invalid network interface selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public ViewPackets(File pcapFile, int replayMode) {
        UITheme.initTheme();
        this.pcapFile = pcapFile;
        this.replayMode = replayMode;
        this.isFileMode = true;
        initComponents();
        jLabel7.setText("  File: " + pcapFile.getName());
    }

    private void initComponents() {
        setTitle("Packet Viewer");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 800);
        getContentPane().setBackground(UITheme.BG_PRIMARY);
        setLayout(new BorderLayout());

        threatDetector = new ThreatDetector();
        sessionId = "session_" + System.currentTimeMillis();
        try { dbManager = new DatabaseManager(); } catch (Exception e) {
            System.err.println("Database init failed: " + e.getMessage());
        }

        // ─── Header ───
        jLabel7 = new JLabel("", SwingConstants.LEFT);
        jLabel7.setFont(UITheme.FONT_SUBTITLE);
        jLabel7.setForeground(UITheme.TEXT_SECONDARY);
        JPanel headerPanel = UITheme.createGradientHeader("📡  PACKET VIEWER", jLabel7);

        // ─── Filter Bar ───
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterPanel.setBackground(UITheme.BG_SECONDARY);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 16, 4, 16)));
        JLabel filterLbl = new JLabel("🔍 Filter (BPF):");
        filterLbl.setFont(UITheme.FONT_BODY_BOLD);
        filterLbl.setForeground(UITheme.ACCENT_LIGHT);
        filterPanel.add(filterLbl);
        filterField = new JTextField(28);
        filterField.setFont(UITheme.FONT_MONO);
        filterField.setToolTipText("e.g., tcp, udp, host 192.168.1.1, port 80");
        filterPanel.add(filterField);

        JButton applyFilterBtn = UITheme.createStyledButton("Apply", UITheme.ACCENT);
        applyFilterBtn.setPreferredSize(new Dimension(100, 34));
        applyFilterBtn.addActionListener(evt -> {
            if (handle == null || !handle.isOpen()) {
                JOptionPane.showMessageDialog(this, "Start capturing first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String expr = filterField.getText().trim();
            if (expr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a BPF filter expression.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                handle.setFilter(expr, BpfProgram.BpfCompileMode.OPTIMIZE);
                JOptionPane.showMessageDialog(this, "Filter applied: " + expr, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (PcapNativeException | NotOpenException e) {
                JOptionPane.showMessageDialog(this, "Invalid BPF expression: " + e.getMessage(), "Filter Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        filterPanel.add(applyFilterBtn);

        JButton clearFilterBtn = UITheme.createStyledButton("Clear", new Color(90, 95, 110));
        clearFilterBtn.setPreferredSize(new Dimension(100, 34));
        clearFilterBtn.addActionListener(evt -> {
            if (handle == null || !handle.isOpen()) return;
            try {
                handle.setFilter("", BpfProgram.BpfCompileMode.OPTIMIZE);
                filterField.setText("");
                JOptionPane.showMessageDialog(this, "Filter cleared.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (PcapNativeException | NotOpenException e) {
                JOptionPane.showMessageDialog(this, "Error clearing filter: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        filterPanel.add(clearFilterBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        // ─── Packet List ───
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> jList1 = UITheme.createStyledList(listModel);
        jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList1.setToolTipText("Select a packet to view details");
        jList1.addListSelectionListener(evt -> {
            int selectedIndex = jList1.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < p.size()) {
                index = selectedIndex;
                jLabel6.setText("Packet " + (index + 1) + ": " + p.get(index).getHeader());
            } else if (selectedIndex >= 0 && selectedIndex == 0 && p.isEmpty()) {
                jLabel6.setText("Waiting for packets...");
            }
        });

        JScrollPane listScrollPane = UITheme.createStyledScrollPane(jList1);
        JPanel listPanel = UITheme.createTitledPanel("Captured Packets", listScrollPane);

        // ─── Charts ───
        JPanel chartsPanel = createChartsPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, chartsPanel);
        splitPane.setDividerLocation(550);
        splitPane.setResizeWeight(0.5);
        splitPane.setBackground(UITheme.BG_PRIMARY);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 12, 5, 12));

        // ─── Alerts ───
        alertsArea = new JTextArea(4, 80);
        alertsArea.setEditable(false);
        alertsArea.setFont(UITheme.FONT_MONO);
        alertsArea.setForeground(UITheme.DANGER);
        alertsArea.setBackground(new Color(40, 20, 20));
        alertsArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane alertsScroll = UITheme.createStyledScrollPane(alertsArea);
        alertsScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(UITheme.DANGER, 1, true),
                        " ⚠ Intrusion Detection Alerts ",
                        javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                        UITheme.FONT_BODY_BOLD, UITheme.DANGER),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        threatDetector.addAlertListener((timestamp, severity, message) -> {
            SwingUtilities.invokeLater(() -> {
                alertsArea.append("[" + timestamp + "] [" + severity + "] " + message + "\n");
                alertsArea.setCaretPosition(alertsArea.getDocument().getLength());
            });
        });

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(UITheme.BG_PRIMARY);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        centerPanel.add(alertsScroll, BorderLayout.SOUTH);

        // ─── Footer ───
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(UITheme.BG_PRIMARY);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 12, 20));

        JPanel footerInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerInfoPanel.setBackground(UITheme.BG_PRIMARY);
        JLabel packetLabel = new JLabel("Selected: ");
        packetLabel.setFont(UITheme.FONT_BODY_BOLD);
        packetLabel.setForeground(UITheme.ACCENT_LIGHT);
        jLabel6 = new JLabel("None");
        jLabel6.setFont(UITheme.FONT_BODY);
        jLabel6.setForeground(UITheme.TEXT_SECONDARY);
        footerInfoPanel.add(packetLabel);
        footerInfoPanel.add(jLabel6);
        footerPanel.add(footerInfoPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttonPanel.setBackground(UITheme.BG_PRIMARY);

        if (isFileMode) {
            JButton loadFileButton = UITheme.createStyledButton("📂  Load PCAP", UITheme.ACCENT);
            loadFileButton.setToolTipText("Load and replay the selected PCAP file");
            loadFileButton.addActionListener(evt -> { loadFileButton.setEnabled(false); loadPcapFile(listModel); });
            buttonPanel.add(loadFileButton);
        } else {
            JButton startCaptureButton = UITheme.createStyledButton("▶  Start Capture", UITheme.SUCCESS);
            startCaptureButton.setToolTipText("Start capturing packets on the selected interface");
            startCaptureButton.addActionListener(evt -> {
                if (device == null) { JOptionPane.showMessageDialog(this, "No interface selected.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                startCaptureButton.setEnabled(false);
                startLiveCapture(listModel, startCaptureButton);
            });
            buttonPanel.add(startCaptureButton);
        }

        JButton searchButton = UITheme.createStyledButton("🔍  Search DB", UITheme.ACCENT);
        searchButton.setToolTipText("Search previously captured packets in the database");
        searchButton.addActionListener(evt -> showSearchDialog());
        buttonPanel.add(searchButton);

        JButton detailedViewButton = UITheme.createStyledButton("📋  Detailed View", UITheme.ACCENT);
        detailedViewButton.setToolTipText("View detailed information of the selected packet");
        detailedViewButton.addActionListener(evt -> {
            if (p.isEmpty()) { JOptionPane.showMessageDialog(this, "No packets captured yet.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (index >= 0 && index < p.size()) { new PacketDetails(p, index, handle).setVisible(true); this.setVisible(false); }
            else { JOptionPane.showMessageDialog(this, "Please select a captured packet.", "Error", JOptionPane.ERROR_MESSAGE); }
        });
        buttonPanel.add(detailedViewButton);

        JButton exitButton = UITheme.createStyledButton("✕  Exit", UITheme.DANGER);
        exitButton.setToolTipText("Exit the application");
        exitButton.addActionListener(evt -> { if (dbManager != null) dbManager.close(); System.exit(0); });
        buttonPanel.add(exitButton);

        footerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        startChartTimer();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBackground(UITheme.BG_SECONDARY);
        panel.setBorder(UITheme.createSectionBorder("Live Traffic Charts"));

        packetsSeries = new XYSeries("Packets/sec");
        XYSeriesCollection lineDataset = new XYSeriesCollection(packetsSeries);
        JFreeChart lineChart = ChartFactory.createXYLineChart(
                "Packets Per Second", "Time (s)", "Count", lineDataset,
                PlotOrientation.VERTICAL, false, true, false);
        styleChart(lineChart);
        ChartPanel linePanel = new ChartPanel(lineChart);
        linePanel.setPreferredSize(new Dimension(400, 200));
        panel.add(linePanel);

        protocolDataset = new DefaultPieDataset();
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Protocol Distribution", protocolDataset, true, true, false);
        styleChart(pieChart);
        if (pieChart.getPlot() instanceof PiePlot) {
            PiePlot pp = (PiePlot) pieChart.getPlot();
            pp.setBackgroundPaint(UITheme.BG_SECONDARY);
            pp.setOutlinePaint(UITheme.BORDER_COLOR);
            pp.setLabelPaint(UITheme.TEXT_PRIMARY);
            pp.setShadowPaint(null);
        }
        ChartPanel piePanel = new ChartPanel(pieChart);
        piePanel.setPreferredSize(new Dimension(400, 200));
        panel.add(piePanel);

        return panel;
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(UITheme.BG_SECONDARY);
        chart.getTitle().setPaint(UITheme.ACCENT_LIGHT);
        chart.getTitle().setFont(UITheme.FONT_BODY_BOLD);
        if (chart.getPlot() instanceof XYPlot) {
            XYPlot plot = chart.getXYPlot();
            plot.setBackgroundPaint(UITheme.BG_PRIMARY);
            plot.setDomainGridlinePaint(UITheme.BORDER_COLOR);
            plot.setRangeGridlinePaint(UITheme.BORDER_COLOR);
            plot.setOutlinePaint(UITheme.BORDER_COLOR);
            plot.getRenderer().setSeriesPaint(0, UITheme.ACCENT);
            plot.getDomainAxis().setTickLabelPaint(UITheme.TEXT_MUTED);
            plot.getDomainAxis().setLabelPaint(UITheme.TEXT_SECONDARY);
            plot.getRangeAxis().setTickLabelPaint(UITheme.TEXT_MUTED);
            plot.getRangeAxis().setLabelPaint(UITheme.TEXT_SECONDARY);
        }
    }

    private void startChartTimer() {
        int interval = ConfigManager.getChartUpdateIntervalMs();
        int maxHistory = ConfigManager.getChartHistorySeconds();
        javax.swing.Timer timer = new javax.swing.Timer(interval, e -> {
            int count = packetsThisSecond.getAndSet(0);
            packetsSeries.add(chartTimeCounter++, count);
            if (packetsSeries.getItemCount() > maxHistory) { packetsSeries.remove(0); }
            protocolDataset.clear();
            for (Map.Entry<String, AtomicInteger> entry : protocolCounts.entrySet()) {
                protocolDataset.setValue(entry.getKey(), entry.getValue().get());
            }
        });
        timer.start();
    }

    private void startLiveCapture(DefaultListModel<String> listModel, JButton startBtn) {
        new Thread(() -> {
            try {
                int snaplen = ConfigManager.getCaptureSnaplen();
                int timeout = ConfigManager.getCaptureTimeout();
                handle = device.openLive(snaplen, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, timeout);

                PacketListener listener = packet -> {
                    p.add(packet);
                    packetsThisSecond.incrementAndGet();
                    String proto = PacketUtils.detectProtocol(packet);
                    protocolCounts.computeIfAbsent(proto, k -> new AtomicInteger(0)).incrementAndGet();
                    threatDetector.analyzePacket(packet);
                    if (dbManager != null) dbManager.savePacket(packet, sessionId);
                    SwingUtilities.invokeLater(() -> listModel.addElement(packet.toString()));
                };

                SwingUtilities.invokeLater(() -> listModel.addElement("[Capturing started… generate some traffic]"));
                int max = -1;
                handle.loop(max, listener);
            } catch (InterruptedException | PcapNativeException | NotOpenException e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, e.getMessage(), "Capture Error", JOptionPane.ERROR_MESSAGE));
            }
        }, "pcap-capture-thread").start();
    }

    private void loadPcapFile(DefaultListModel<String> listModel) {
        JProgressBar progressBar = UITheme.createStyledProgressBar("Loading PCAP file...");
        progressBar.setIndeterminate(true);

        JDialog progressDialog = new JDialog(this, "Loading...", false);
        progressDialog.setSize(350, 90);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.getContentPane().setBackground(UITheme.BG_SECONDARY);
        progressDialog.add(progressBar);
        progressDialog.setVisible(true);

        new Thread(() -> {
            try {
                handle = Pcaps.openOffline(pcapFile.getAbsolutePath());
                Packet packet;
                List<Packet> allPackets = new ArrayList<>();
                while ((packet = handle.getNextPacket()) != null) { allPackets.add(packet); }
                SwingUtilities.invokeLater(() -> progressBar.setMaximum(allPackets.size()));
                SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(false));

                for (int idx = 0; idx < allPackets.size(); idx++) {
                    Packet pkt = allPackets.get(idx);
                    p.add(pkt);
                    packetsThisSecond.incrementAndGet();
                    String proto = PacketUtils.detectProtocol(pkt);
                    protocolCounts.computeIfAbsent(proto, k -> new AtomicInteger(0)).incrementAndGet();
                    threatDetector.analyzePacket(pkt);
                    final int fi = idx;
                    SwingUtilities.invokeLater(() -> { listModel.addElement(pkt.toString()); progressBar.setValue(fi + 1); });
                    if (replayMode == 0) Thread.sleep(1000);
                    else if (replayMode == 1) Thread.sleep(200);
                }
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(this, "Loaded " + allPackets.size() + " packets from file.", "Done", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(this, "Error loading PCAP file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "pcap-file-reader").start();
    }

    private void showSearchDialog() {
        JDialog dialog = new JDialog(this, "Search Captured Packets", true);
        dialog.setSize(850, 580);
        dialog.getContentPane().setBackground(UITheme.BG_PRIMARY);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(this);

        JPanel dialogHeader = UITheme.createGradientHeader("🔍  Search Packets");
        dialog.add(dialogHeader, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(UITheme.BG_SECONDARY);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                UITheme.createSectionBorder("Search Filters"),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel ipLbl = new JLabel("IP Address:");
        ipLbl.setForeground(UITheme.ACCENT_LIGHT);
        ipLbl.setFont(UITheme.FONT_BODY_BOLD);
        searchPanel.add(ipLbl, gbc);
        JTextField ipField = new JTextField(15);
        ipField.setFont(UITheme.FONT_MONO);
        gbc.gridx = 1;
        searchPanel.add(ipField, gbc);

        gbc.gridx = 2;
        JLabel protoLbl = new JLabel("Protocol:");
        protoLbl.setForeground(UITheme.ACCENT_LIGHT);
        protoLbl.setFont(UITheme.FONT_BODY_BOLD);
        searchPanel.add(protoLbl, gbc);
        JComboBox<String> protocolCombo = new JComboBox<>(new String[]{"All", "TCP", "UDP", "ICMP", "ARP", "Other"});
        gbc.gridx = 3;
        searchPanel.add(protocolCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel fromLbl = new JLabel("Date From:");
        fromLbl.setForeground(UITheme.ACCENT_LIGHT);
        fromLbl.setFont(UITheme.FONT_BODY_BOLD);
        searchPanel.add(fromLbl, gbc);
        JTextField dateFromField = new JTextField(12);
        dateFromField.setFont(UITheme.FONT_MONO);
        dateFromField.setToolTipText("yyyy-MM-dd");
        gbc.gridx = 1;
        searchPanel.add(dateFromField, gbc);

        gbc.gridx = 2;
        JLabel toLbl = new JLabel("Date To:");
        toLbl.setForeground(UITheme.ACCENT_LIGHT);
        toLbl.setFont(UITheme.FONT_BODY_BOLD);
        searchPanel.add(toLbl, gbc);
        JTextField dateToField = new JTextField(12);
        dateToField.setFont(UITheme.FONT_MONO);
        dateToField.setToolTipText("yyyy-MM-dd");
        gbc.gridx = 3;
        searchPanel.add(dateToField, gbc);

        String[] cols = {"Timestamp", "Src IP", "Dst IP", "Protocol", "Length", "Summary"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0);
        JTable resultTable = UITheme.createStyledTable(tableModel);

        JButton searchBtn = UITheme.createStyledButton("🔍 Search", UITheme.ACCENT);
        searchBtn.setPreferredSize(new Dimension(120, 60));
        gbc.gridx = 4; gbc.gridy = 0; gbc.gridheight = 2;
        searchBtn.addActionListener(e -> {
            tableModel.setRowCount(0);
            DatabaseManager db = new DatabaseManager();
            List<String[]> results = db.searchPackets(
                    ipField.getText(), (String) protocolCombo.getSelectedItem(),
                    dateFromField.getText(), dateToField.getText());
            db.close();
            for (String[] row : results) { tableModel.addRow(row); }
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No matching packets found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        searchPanel.add(searchBtn, gbc);

        JPanel midPanel = new JPanel(new BorderLayout(0, 8));
        midPanel.setBackground(UITheme.BG_PRIMARY);
        midPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 4, 16));
        midPanel.add(searchPanel, BorderLayout.NORTH);
        midPanel.add(UITheme.createStyledScrollPane(resultTable), BorderLayout.CENTER);
        dialog.add(midPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottomPanel.setBackground(UITheme.BG_PRIMARY);
        JButton closeBtn = UITheme.createStyledButton("✕  Close", UITheme.DANGER);
        closeBtn.addActionListener(e -> dialog.dispose());
        bottomPanel.add(closeBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
