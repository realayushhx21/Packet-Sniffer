package com.first.panels;

import com.first.*;
import com.first.components.*;
import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    public SettingsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("⚙ Settings & Configuration");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 2, 12, 12));
        grid.setOpaque(false);

        // Capture settings
        grid.add(createSettingCard("📡 Capture Settings",
            new String[]{"Snap Length", "Timeout (ms)"},
            new String[]{String.valueOf(ConfigManager.getCaptureSnaplen()),
                         String.valueOf(ConfigManager.getCaptureTimeout())},
            new String[]{"capture.snaplen", "capture.timeout"}));

        // Chart settings
        grid.add(createSettingCard("📊 Chart Settings",
            new String[]{"History (seconds)", "Update Interval (ms)"},
            new String[]{String.valueOf(ConfigManager.getChartHistorySeconds()),
                         String.valueOf(ConfigManager.getChartUpdateIntervalMs())},
            new String[]{"chart.history.seconds", "chart.update.interval.ms"}));

        // Threat settings
        grid.add(createSettingCard("🛡 Threat Detection",
            new String[]{"Port Scan Threshold", "Port Scan Window (s)", "DoS Threshold", "DoS Window (s)"},
            new String[]{String.valueOf(ConfigManager.getPortScanThreshold()),
                         String.valueOf(ConfigManager.getPortScanWindowSeconds()),
                         String.valueOf(ConfigManager.getDosThreshold()),
                         String.valueOf(ConfigManager.getDosWindowSeconds())},
            new String[]{"threat.portscan.threshold", "threat.portscan.window.seconds",
                         "threat.dos.threshold", "threat.dos.window.seconds"}));

        // AI settings
        GlassCard aiCard = new GlassCard(UITheme.ACCENT_PURPLE);
        aiCard.setLayout(new BoxLayout(aiCard, BoxLayout.Y_AXIS));
        JLabel aiTitle = new JLabel("  🤖 AI Configuration");
        aiTitle.setFont(UITheme.FONT_BODY_BOLD);
        aiTitle.setForeground(UITheme.ACCENT_PURPLE);
        aiTitle.setAlignmentX(LEFT_ALIGNMENT);
        aiCard.add(aiTitle);
        aiCard.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel keyLbl = new JLabel("  Gemini API Key:");
        keyLbl.setFont(UITheme.FONT_BODY);
        keyLbl.setForeground(UITheme.TEXT_SECONDARY);
        keyLbl.setAlignmentX(LEFT_ALIGNMENT);
        aiCard.add(keyLbl);

        JPasswordField keyField = new JPasswordField(ConfigManager.getGeminiApiKey());
        keyField.setFont(UITheme.FONT_MONO);
        keyField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        keyField.setAlignmentX(LEFT_ALIGNMENT);
        aiCard.add(keyField);
        aiCard.add(Box.createRigidArea(new Dimension(0, 8)));

        JButton saveKeyBtn = UITheme.createStyledButton("Save API Key", UITheme.ACCENT_PURPLE);
        saveKeyBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveKeyBtn.setMaximumSize(new Dimension(160, 32));
        saveKeyBtn.addActionListener(e -> {
            String key = new String(keyField.getPassword());
            ConfigManager.setProperty("gemini.api.key", key);
            NotificationManager.show("API key saved", NotificationManager.NotificationType.SUCCESS);
        });
        aiCard.add(saveKeyBtn);
        grid.add(aiCard);

        // Database info
        GlassCard dbCard = new GlassCard(UITheme.ACCENT);
        dbCard.setLayout(new BoxLayout(dbCard, BoxLayout.Y_AXIS));
        JLabel dbTitle = new JLabel("  💾 Database");
        dbTitle.setFont(UITheme.FONT_BODY_BOLD);
        dbTitle.setForeground(UITheme.ACCENT);
        dbTitle.setAlignmentX(LEFT_ALIGNMENT);
        dbCard.add(dbTitle);
        dbCard.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel dbPath = new JLabel("  Path: " + ConfigManager.getDbPath());
        dbPath.setFont(UITheme.FONT_MONO);
        dbPath.setForeground(UITheme.TEXT_SECONDARY);
        dbPath.setAlignmentX(LEFT_ALIGNMENT);
        dbCard.add(dbPath);

        java.io.File dbFile = new java.io.File(ConfigManager.getDbPath());
        JLabel dbSize = new JLabel("  Size: " + (dbFile.exists() ? (dbFile.length() / 1024) + " KB" : "N/A"));
        dbSize.setFont(UITheme.FONT_MONO);
        dbSize.setForeground(UITheme.TEXT_MUTED);
        dbSize.setAlignmentX(LEFT_ALIGNMENT);
        dbCard.add(dbSize);
        grid.add(dbCard);

        // System info
        GlassCard sysCard = new GlassCard(UITheme.WARNING);
        sysCard.setLayout(new BoxLayout(sysCard, BoxLayout.Y_AXIS));
        JLabel sysTitle = new JLabel("  🖥 System Info");
        sysTitle.setFont(UITheme.FONT_BODY_BOLD);
        sysTitle.setForeground(UITheme.WARNING);
        sysTitle.setAlignmentX(LEFT_ALIGNMENT);
        sysCard.add(sysTitle);
        sysCard.add(Box.createRigidArea(new Dimension(0, 10)));

        Runtime rt = Runtime.getRuntime();
        addInfoLine(sysCard, "OS: " + System.getProperty("os.name"));
        addInfoLine(sysCard, "Java: " + System.getProperty("java.version"));
        addInfoLine(sysCard, "Cores: " + rt.availableProcessors());
        addInfoLine(sysCard, "Max Memory: " + (rt.maxMemory() / (1024*1024)) + " MB");
        grid.add(sysCard);

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BG_PRIMARY);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createSettingCard(String title, String[] labels, String[] values, String[] keys) {
        GlassCard card = new GlassCard(UITheme.ACCENT);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("  " + title);
        titleLbl.setFont(UITheme.FONT_BODY_BOLD);
        titleLbl.setForeground(UITheme.ACCENT);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel("  " + labels[i] + ":");
            lbl.setFont(UITheme.FONT_SMALL);
            lbl.setForeground(UITheme.TEXT_SECONDARY);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            card.add(lbl);

            fields[i] = new JTextField(values[i]);
            fields[i].setFont(UITheme.FONT_MONO);
            fields[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            fields[i].setAlignmentX(LEFT_ALIGNMENT);
            card.add(fields[i]);
            card.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        JButton saveBtn = UITheme.createStyledButton("Save", UITheme.ACCENT);
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(100, 28));
        saveBtn.addActionListener(e -> {
            for (int i = 0; i < keys.length; i++) {
                ConfigManager.setProperty(keys[i], fields[i].getText().trim());
            }
            NotificationManager.show("Settings saved", NotificationManager.NotificationType.SUCCESS);
        });
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(saveBtn);

        return card;
    }

    private void addInfoLine(JPanel card, String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(UITheme.FONT_MONO_SM);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createRigidArea(new Dimension(0, 3)));
    }
}
