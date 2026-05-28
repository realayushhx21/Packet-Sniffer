package com.first.panels;

import com.first.*;
import com.first.components.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class DatabasePanel extends JPanel {
    public DatabasePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("💾 Database Logs & Session Manager");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.ACCENT);
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);

        // ─── Search Tab ───
        JPanel searchTab = new JPanel(new BorderLayout(0, 8));
        searchTab.setBackground(UITheme.BG_PRIMARY);
        searchTab.setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 8));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filters.setOpaque(false);

        filters.add(lbl("IP:"));
        JTextField ipField = UITheme.createSearchField("IP address");
        ipField.setPreferredSize(new Dimension(150, 30));
        filters.add(ipField);

        filters.add(lbl("Protocol:"));
        JComboBox<String> protoCb = new JComboBox<>(new String[]{"All", "TCP", "UDP", "ICMP", "ARP", "Other"});
        protoCb.setFont(UITheme.FONT_MONO);
        filters.add(protoCb);

        filters.add(lbl("From:"));
        JTextField fromField = UITheme.createSearchField("yyyy-MM-dd");
        fromField.setPreferredSize(new Dimension(120, 30));
        filters.add(fromField);

        filters.add(lbl("To:"));
        JTextField toField = UITheme.createSearchField("yyyy-MM-dd");
        toField.setPreferredSize(new Dimension(120, 30));
        filters.add(toField);

        String[] cols = {"Timestamp", "Src IP", "Dst IP", "Protocol", "Length", "Summary"};
        DefaultTableModel searchModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable searchTable = UITheme.createStyledTable(searchModel);

        JButton searchBtn = UITheme.createStyledButton("🔍 Search", UITheme.ACCENT);
        searchBtn.setPreferredSize(new Dimension(120, 30));
        searchBtn.addActionListener(e -> {
            searchModel.setRowCount(0);
            DatabaseManager db = new DatabaseManager();
            List<String[]> results = db.searchPackets(
                ipField.getText(), (String) protoCb.getSelectedItem(),
                fromField.getText(), toField.getText());
            db.close();
            for (String[] row : results) searchModel.addRow(row);
            if (results.isEmpty())
                JOptionPane.showMessageDialog(this, "No matching packets found.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        filters.add(searchBtn);

        searchTab.add(filters, BorderLayout.NORTH);
        searchTab.add(UITheme.createStyledScrollPane(searchTable), BorderLayout.CENTER);
        tabs.addTab("🔍 Search Packets", searchTab);

        // ─── Sessions Tab ───
        JPanel sessionsTab = new JPanel(new BorderLayout(0, 8));
        sessionsTab.setBackground(UITheme.BG_PRIMARY);
        sessionsTab.setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 8));

        String[] sessCols = {"Session ID", "Start Time", "Packets"};
        DefaultTableModel sessModel = new DefaultTableModel(sessCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable sessTable = UITheme.createStyledTable(sessModel);

        JPanel sessBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        sessBtn.setOpaque(false);

        JButton loadSessBtn = UITheme.createStyledButton("↻ Load Sessions", UITheme.ACCENT);
        loadSessBtn.setPreferredSize(new Dimension(160, 30));
        loadSessBtn.addActionListener(e -> {
            sessModel.setRowCount(0);
            DatabaseManager db = new DatabaseManager();
            List<String[]> sessions = db.getSessions();
            db.close();
            for (String[] s : sessions) sessModel.addRow(s);
        });
        sessBtn.add(loadSessBtn);

        JButton viewSessBtn = UITheme.createStyledButton("📂 View Session", UITheme.SUCCESS);
        viewSessBtn.setPreferredSize(new Dimension(160, 30));
        viewSessBtn.addActionListener(e -> {
            int row = sessTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a session.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            String sid = (String) sessTable.getValueAt(row, 0);
            DatabaseManager db = new DatabaseManager();
            List<String[]> pkts = db.loadSession(sid);
            db.close();
            if (pkts.isEmpty()) { JOptionPane.showMessageDialog(this, "No packets in this session.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
            showSessionDialog(sid, pkts);
        });
        sessBtn.add(viewSessBtn);

        sessionsTab.add(sessBtn, BorderLayout.NORTH);
        sessionsTab.add(UITheme.createStyledScrollPane(sessTable), BorderLayout.CENTER);
        tabs.addTab("💾 Previous Sessions", sessionsTab);

        GlassCard tabCard = new GlassCard(UITheme.ACCENT);
        tabCard.setLayout(new BorderLayout());
        tabCard.add(tabs, BorderLayout.CENTER);
        add(tabCard, BorderLayout.CENTER);
    }

    private void showSessionDialog(String sessionId, List<String[]> pkts) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Session: " + sessionId, true);
        dlg.setSize(900, 550);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.BG_PRIMARY);
        dlg.setLayout(new BorderLayout(8, 8));

        JPanel header = UITheme.createGradientHeader("📊 Session: " + sessionId);
        JLabel cnt = new JLabel(pkts.size() + " packets", SwingConstants.CENTER);
        cnt.setFont(UITheme.FONT_SUBTITLE);
        cnt.setForeground(UITheme.TEXT_SECONDARY);
        header = UITheme.createGradientHeader("📊 Session: " + sessionId, cnt);
        dlg.add(header, BorderLayout.NORTH);

        String[] c = {"Timestamp", "Src IP", "Dst IP", "Protocol", "Length", "Summary"};
        DefaultTableModel m = new DefaultTableModel(pkts.toArray(new String[0][]), c) {
            @Override public boolean isCellEditable(int r, int col) { return false; }
        };
        JTable t = UITheme.createStyledTable(m);
        JPanel tp = new JPanel(new BorderLayout());
        tp.setBackground(UITheme.BG_PRIMARY);
        tp.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        tp.add(UITheme.createStyledScrollPane(t));
        dlg.add(tp, BorderLayout.CENTER);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bp.setBackground(UITheme.BG_PRIMARY);
        JButton cl = UITheme.createStyledButton("✕ Close", UITheme.DANGER);
        cl.addActionListener(e -> dlg.dispose());
        bp.add(cl);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY_BOLD);
        l.setForeground(UITheme.ACCENT);
        return l;
    }
}
