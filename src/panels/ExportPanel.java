package com.first.panels;

import com.first.*;
import com.first.components.*;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.packet.Packet;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ExportPanel extends JPanel {
    private final List<Packet> packets;
    private PcapHandle handle;

    public ExportPanel(List<Packet> packets) {
        this.packets = packets;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("📤 Export Center");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.SUCCESS);
        add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);

        cards.add(createExportCard("📄", "CSV Export", "Export packets as comma-separated values",
                UITheme.ACCENT, "CSV"));
        cards.add(createExportCard("📋", "JSON Export", "Export packets in JSON format",
                UITheme.ACCENT_PURPLE, "JSON"));
        cards.add(createExportCard("📦", "PCAP Export", "Export as native PCAP capture file",
                UITheme.SUCCESS, "PCAP"));

        add(cards, BorderLayout.CENTER);

        // Info panel
        GlassCard info = new GlassCard(UITheme.BORDER_COLOR);
        info.setLayout(new BorderLayout());
        info.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel infoLbl = new JLabel("ℹ  Available packets for export: " + packets.size());
        infoLbl.setFont(UITheme.FONT_BODY);
        infoLbl.setForeground(UITheme.TEXT_SECONDARY);
        info.add(infoLbl);
        info.setPreferredSize(new Dimension(0, 60));
        add(info, BorderLayout.SOUTH);
    }

    private JPanel createExportCard(String icon, String title, String desc, Color accent, String format) {
        GlassCard card = new GlassCard(accent);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30, 24, 30, 24));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLbl);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        JLabel descLbl = new JLabel("<html><center>" + desc + "</center></html>");
        descLbl.setFont(UITheme.FONT_SMALL);
        descLbl.setForeground(UITheme.TEXT_MUTED);
        descLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(descLbl);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton exportBtn = UITheme.createStyledButton("Export " + format, accent);
        exportBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        exportBtn.setPreferredSize(new Dimension(160, 36));
        exportBtn.setMaximumSize(new Dimension(160, 36));
        exportBtn.addActionListener(e -> doExport(format));
        card.add(exportBtn);

        return card;
    }

    public void setHandle(PcapHandle h) { this.handle = h; }

    private void doExport(String format) {
        if (packets == null || packets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No packets to export.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String ext = format.equalsIgnoreCase("CSV") ? "csv" : format.equalsIgnoreCase("JSON") ? "json" : "pcap";
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("capture." + ext));
        chooser.setDialogTitle("Export as " + format);
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        File target = chooser.getSelectedFile();

        try {
            switch (format) {
                case "CSV": ExportManager.exportCSV(packets, target); break;
                case "JSON": ExportManager.exportJSON(packets, target); break;
                case "PCAP":
                    if (handle == null) {
                        JOptionPane.showMessageDialog(this, "PCAP export requires an active capture handle.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    ExportManager.exportPCAP(packets, handle, target);
                    break;
            }
            NotificationManager.show("Exported " + packets.size() + " packets to " + target.getName(),
                    NotificationManager.NotificationType.SUCCESS);
            JOptionPane.showMessageDialog(this,
                    "Exported " + packets.size() + " packets to:\n" + target.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
