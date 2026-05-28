package com.first;

import com.first.components.*;
import com.first.panels.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.pcap4j.packet.Packet;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DashboardFrame extends JFrame {

    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final SidebarNav sidebar;
    private final StatusBar statusBar;

    // Shared state
    private final List<Packet> packets = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger packetsThisSecond = new AtomicInteger(0);
    private final Map<String, AtomicInteger> protocolCounts = new ConcurrentHashMap<>();
    private final XYSeries packetsSeries = new XYSeries("Packets/sec");
    private final DefaultPieDataset protocolDataset = new DefaultPieDataset();
    private final ThreatDetector threatDetector = new ThreatDetector();
    private DatabaseManager dbManager;
    private final String sessionId = "session_" + System.currentTimeMillis();
    private int chartTimeCounter = 0;

    // Panels
    private DashboardPanel dashboardPanel;
    private LiveCapturePanel liveCapturePanel;
    private ThreatPanel threatPanel;
    private AIAnalysisPanel aiPanel;
    private PacketExplorerPanel packetExplorerPanel;
    private ExportPanel exportPanel;

    public DashboardFrame() {
        UITheme.initTheme();

        setTitle("Packet Sniffer — Network Security Intelligence Platform");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1400, 850);
        setMinimumSize(new Dimension(1100, 700));
        getContentPane().setBackground(UITheme.BG_PRIMARY);
        setLayout(new BorderLayout());

        try { dbManager = new DatabaseManager(); } catch (Exception e) {
            System.err.println("Database init failed: " + e.getMessage());
        }

        // Content area
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(UITheme.BG_PRIMARY);

        // Sidebar
        sidebar = new SidebarNav(contentLayout, contentPanel);

        // Status bar
        statusBar = new StatusBar();
        statusBar.setDbStatus(dbManager != null);

        // Initialize NotificationManager
        JLayeredPane layeredPane = getLayeredPane();
        NotificationManager.init(layeredPane);

        // Create panels
        createPanels();

        // Add sidebar nav items
        sidebar.addNavItem("🏠", "Dashboard", "dashboard");
        sidebar.addNavItem("📡", "Live Capture", "capture");
        sidebar.addNavItem("🛡", "Threat Monitor", "threats");
        sidebar.addNavItem("🤖", "AI Analysis", "ai");
        sidebar.addNavItem("📋", "Packet Explorer", "explorer");
        sidebar.addNavItem("💾", "Database Logs", "database");
        sidebar.addNavItem("📤", "Export Center", "export");
        sidebar.addNavItem("📊", "Analytics", "analytics");
        sidebar.addNavItem("⚙", "Settings", "settings");

        // Layout
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        // Wire up threat detector -> notifications + threat panel
        threatDetector.addAlertListener((timestamp, severity, message) -> {
            SwingUtilities.invokeLater(() -> {
                threatPanel.addAlert(timestamp, severity, message);
                dashboardPanel.incrementThreats();
                NotificationManager.show(message,
                    severity.equals(ThreatDetector.SEVERITY_HIGH)
                        ? NotificationManager.NotificationType.CRITICAL
                        : NotificationManager.NotificationType.WARNING);
            });
        });

        // Wire capture panel callbacks
        liveCapturePanel.setOnPacketCallback(() -> {
            statusBar.setTotalPackets(packets.size());
        });
        liveCapturePanel.setOnCaptureStarted(() -> {
            statusBar.setCaptureActive(true);
            dashboardPanel.setCaptureStatus("Active");
        });

        // Wire packet table double-click to explorer
        liveCapturePanel.getPacketTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = liveCapturePanel.getPacketTable().getSelectedRow();
                    if (row >= 0 && row < packets.size()) {
                        packetExplorerPanel.setHandle(liveCapturePanel.getHandle());
                        packetExplorerPanel.inspectPacket(row);
                        sidebar.selectItem(4, "explorer");
                    }
                }
            }
        });

        // Chart update timer
        startChartTimer();

        setLocationRelativeTo(null);
    }

    private void createPanels() {
        dashboardPanel = new DashboardPanel(packetsSeries, protocolDataset);
        contentPanel.add(dashboardPanel, "dashboard");

        liveCapturePanel = new LiveCapturePanel(packets, packetsThisSecond, protocolCounts,
                threatDetector, dbManager, sessionId);
        contentPanel.add(liveCapturePanel, "capture");

        threatPanel = new ThreatPanel();
        contentPanel.add(threatPanel, "threats");

        aiPanel = new AIAnalysisPanel(packets);
        contentPanel.add(aiPanel, "ai");

        packetExplorerPanel = new PacketExplorerPanel(packets);
        contentPanel.add(packetExplorerPanel, "explorer");

        DatabasePanel databasePanel = new DatabasePanel();
        contentPanel.add(databasePanel, "database");

        exportPanel = new ExportPanel(packets);
        contentPanel.add(exportPanel, "export");

        AnalyticsPanel analyticsPanel = new AnalyticsPanel(packetsSeries, protocolDataset, packets, protocolCounts);
        contentPanel.add(analyticsPanel, "analytics");

        SettingsPanel settingsPanel = new SettingsPanel();
        contentPanel.add(settingsPanel, "settings");
    }

    private void startChartTimer() {
        int interval = ConfigManager.getChartUpdateIntervalMs();
        int maxHistory = ConfigManager.getChartHistorySeconds();
        javax.swing.Timer timer = new javax.swing.Timer(interval, e -> {
            int count = packetsThisSecond.getAndSet(0);
            packetsSeries.add(chartTimeCounter++, count);
            if (packetsSeries.getItemCount() > maxHistory) packetsSeries.remove(0);

            protocolDataset.clear();
            for (Map.Entry<String, AtomicInteger> entry : protocolCounts.entrySet()) {
                protocolDataset.setValue(entry.getKey(), entry.getValue().get());
            }

            // Update dashboard metrics
            dashboardPanel.updateMetrics(count, packets.size(),
                    dashboardPanel.getTotalThreats(), protocolCounts.size());

            // Update status bar
            statusBar.setPacketRate(count);
            statusBar.setTotalPackets(packets.size());
            statusBar.setCaptureActive(liveCapturePanel.isCapturing());

            // Update export handle
            exportPanel.setHandle(liveCapturePanel.getHandle());
        });
        timer.start();
    }

    @Override
    public void dispose() {
        if (dbManager != null) dbManager.close();
        super.dispose();
    }
}
