package com.first.panels;

import com.first.*;
import com.first.components.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.*;
import org.pcap4j.packet.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalyticsPanel extends JPanel {
    private final XYSeries packetsSeries;
    private final DefaultPieDataset protocolDataset;
    private final List<Packet> packets;
    private final Map<String, AtomicInteger> protocolCounts;
    private final DefaultPieDataset ipDataset;
    private final XYSeries sizeSeries;

    public AnalyticsPanel(XYSeries packetsSeries, DefaultPieDataset protocolDataset,
                          List<Packet> packets, Map<String, AtomicInteger> protocolCounts) {
        this.packetsSeries = packetsSeries;
        this.protocolDataset = protocolDataset;
        this.packets = packets;
        this.protocolCounts = protocolCounts;

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = new JLabel("📊 Network Analytics");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.ACCENT_PURPLE);
        titleRow.add(title, BorderLayout.WEST);

        JButton refreshBtn = UITheme.createStyledButton("↻ Refresh", UITheme.ACCENT);
        refreshBtn.setPreferredSize(new Dimension(120, 30));
        refreshBtn.addActionListener(e -> refreshCharts());
        titleRow.add(refreshBtn, BorderLayout.EAST);
        add(titleRow, BorderLayout.NORTH);

        JPanel chartsGrid = new JPanel(new GridLayout(2, 2, 12, 12));
        chartsGrid.setOpaque(false);

        // Traffic line chart
        XYSeriesCollection lineDS = new XYSeriesCollection(packetsSeries);
        JFreeChart lineChart = ChartFactory.createXYLineChart(
                "Live Traffic Rate", "Time (s)", "Packets/s", lineDS,
                PlotOrientation.VERTICAL, false, true, false);
        styleChart(lineChart);
        chartsGrid.add(wrapChart(lineChart, "📈 Traffic Rate", UITheme.ACCENT));

        // Protocol pie
        // JFreeChart pieChart = ChartFactory.createPieChart("Protocol Distribution", protocolDataset, true, true, false);
        // styleChart(pieChart);
        // if (pieChart.getPlot() instanceof PiePlot) {
        //     PiePlot pp = (PiePlot) pieChart.getPlot();
        //     pp.setBackgroundPaint(UITheme.BG_SECONDARY);
        //     pp.setOutlinePaint(UITheme.BORDER_COLOR);
        //     pp.setLabelPaint(UITheme.TEXT_PRIMARY);
        //     pp.setShadowPaint(null);
        //     pp.setLabelFont(UITheme.FONT_SMALL);
        // }
        // chartsGrid.add(wrapChart(pieChart, "🔗 Protocol Distribution", UITheme.ACCENT_PURPLE));

        JFreeChart pieChart = ChartFactory.createPieChart(
        "Protocol Distribution",
        protocolDataset,
        true,
        true,
        false
);

styleChart(pieChart);

if (pieChart.getPlot() instanceof PiePlot) {
    PiePlot pp = (PiePlot) pieChart.getPlot();

    pp.setBackgroundPaint(UITheme.BG_SECONDARY);
    pp.setOutlinePaint(UITheme.BORDER_COLOR);

    // Label text color
    pp.setLabelPaint(Color.WHITE);

    // Label background
    pp.setLabelBackgroundPaint(new Color(40, 40, 40));

    // Remove shadow
    pp.setShadowPaint(null);

    // Font
    pp.setLabelFont(UITheme.FONT_SMALL);

    // Optional: label outline
    pp.setLabelOutlinePaint(Color.DARK_GRAY);
    pp.setLabelShadowPaint(null);

    // Better visible section colors
    pp.setSectionPaint("TCP", new Color(52, 152, 219));
    pp.setSectionPaint("UDP", new Color(231, 76, 60));
    pp.setSectionPaint("HTTP", new Color(46, 204, 113));
    pp.setSectionPaint("HTTPS", new Color(155, 89, 182));
}

chartsGrid.add(
    wrapChart(
        pieChart,
        "🔗 Protocol Distribution",
        UITheme.ACCENT_PURPLE
    )
);
        // Top IPs chart
        ipDataset = new DefaultPieDataset();
        JFreeChart ipChart = ChartFactory.createPieChart("Top Source IPs", ipDataset, true, true, false);
        styleChart(ipChart);
        // if (ipChart.getPlot() instanceof PiePlot) {
        //     PiePlot pp = (PiePlot) ipChart.getPlot();
        //     pp.setBackgroundPaint(UITheme.BG_SECONDARY);
        //     pp.setOutlinePaint(UITheme.BORDER_COLOR);
        //     pp.setLabelPaint(UITheme.TEXT_PRIMARY);
        //     pp.setShadowPaint(null);
        //     pp.setLabelFont(UITheme.FONT_SMALL);
        // }
        if (ipChart.getPlot() instanceof PiePlot) {
    PiePlot pp = (PiePlot) ipChart.getPlot();

    pp.setBackgroundPaint(UITheme.BG_SECONDARY);
    pp.setOutlinePaint(UITheme.BORDER_COLOR);

    // Label styling
    pp.setLabelPaint(Color.WHITE);
    pp.setLabelBackgroundPaint(new Color(35, 35, 35));
    pp.setLabelOutlinePaint(Color.DARK_GRAY);
    pp.setLabelShadowPaint(null);

    // Remove chart shadow
    pp.setShadowPaint(null);

    // Font
    pp.setLabelFont(UITheme.FONT_SMALL);

    // Optional cleaner look
    pp.setSimpleLabels(true);

    // Better visible slice colors
    pp.setSectionPaint("192.168.1.1", new Color(52, 152, 219));
    pp.setSectionPaint("10.0.0.5", new Color(231, 76, 60));
    pp.setSectionPaint("172.16.0.2", new Color(46, 204, 113));
}
        chartsGrid.add(wrapChart(ipChart, "🌐 Top Source IPs", UITheme.SUCCESS));

        // Packet size distribution
        sizeSeries = new XYSeries("Size");
        XYSeriesCollection sizeDS = new XYSeriesCollection(sizeSeries);
        JFreeChart sizeChart = ChartFactory.createXYLineChart(
                "Packet Sizes", "Packet #", "Bytes", sizeDS,
                PlotOrientation.VERTICAL, false, true, false);
        styleChart(sizeChart);
        chartsGrid.add(wrapChart(sizeChart, "📏 Packet Sizes", UITheme.WARNING));

        add(chartsGrid, BorderLayout.CENTER);

        // Auto-refresh every 2 seconds
        javax.swing.Timer autoRefresh = new javax.swing.Timer(2000, e -> refreshCharts());
        autoRefresh.start();
    }

    private JPanel wrapChart(JFreeChart chart, String label, Color accent) {
        GlassCard card = new GlassCard(accent);
        card.setLayout(new BorderLayout());
        JLabel lbl = new JLabel("  " + label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(accent);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        card.add(lbl, BorderLayout.NORTH);
        ChartPanel cp = new ChartPanel(chart);
        cp.setMinimumDrawWidth(0);
        cp.setMinimumDrawHeight(0);
        card.add(cp, BorderLayout.CENTER);
        return card;
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
        if (chart.getTitle() != null) chart.getTitle().setVisible(false);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(UITheme.BG_SECONDARY);
            chart.getLegend().setItemPaint(UITheme.TEXT_SECONDARY);
            chart.getLegend().setItemFont(UITheme.FONT_TINY);
        }
        if (chart.getPlot() instanceof XYPlot) {
            XYPlot plot = chart.getXYPlot();
            plot.setBackgroundPaint(UITheme.BG_PRIMARY);
            plot.setDomainGridlinePaint(new Color(30, 41, 59));
            plot.setRangeGridlinePaint(new Color(30, 41, 59));
            plot.setOutlinePaint(UITheme.BORDER_COLOR);
            plot.getRenderer().setSeriesPaint(0, UITheme.ACCENT);
            plot.getDomainAxis().setTickLabelPaint(UITheme.TEXT_MUTED);
            plot.getDomainAxis().setLabelPaint(UITheme.TEXT_SECONDARY);
            plot.getDomainAxis().setTickLabelFont(UITheme.FONT_TINY);
            plot.getRangeAxis().setTickLabelPaint(UITheme.TEXT_MUTED);
            plot.getRangeAxis().setLabelPaint(UITheme.TEXT_SECONDARY);
            plot.getRangeAxis().setTickLabelFont(UITheme.FONT_TINY);
        }
    }

    private void refreshCharts() {
        if (packets.isEmpty()) return;

        // --- Update Top Source IPs ---
        Map<String, Integer> ipCounts = new LinkedHashMap<>();
        synchronized (packets) {
            for (Packet p : packets) {
                IpV4Packet ipv4 = p.get(IpV4Packet.class);
                if (ipv4 != null && ipv4.getHeader() != null) {
                    String src = ipv4.getHeader().getSrcAddr().getHostAddress();
                    ipCounts.merge(src, 1, Integer::sum);
                }
            }
        }
        // Sort by count descending, take top 8
        ipDataset.clear();
        ipCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(8)
            .forEach(e -> ipDataset.setValue(e.getKey(), e.getValue()));

        // --- Update Packet Sizes ---
        sizeSeries.clear();
        int total;
        synchronized (packets) { total = packets.size(); }
        // Sample up to 200 points evenly
        int step = Math.max(1, total / 200);
        synchronized (packets) {
            for (int i = 0; i < total; i += step) {
                try {
                    sizeSeries.add(i + 1, packets.get(i).length());
                } catch (Exception ignored) {}
            }
        }
    }
}
