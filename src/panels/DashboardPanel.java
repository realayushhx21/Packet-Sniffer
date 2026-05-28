package com.first.panels;

import com.first.*;
import com.first.components.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.*;
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DashboardPanel extends JPanel {
    private final MetricCard packetsPerSec;
    private final MetricCard totalPacketsCard;
    private final MetricCard threatCountCard;
    private final MetricCard protocolCard;
    private final MetricCard captureStatusCard;
    private final MetricCard networkCard;
    private int totalThreats = 0;

    public DashboardPanel(XYSeries packetsSeries, DefaultPieDataset protocolDataset) {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Page title
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Real-time network security monitoring");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(title);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 2)));
        titlePanel.add(subtitle);
        add(titlePanel, BorderLayout.NORTH);

        // Metric cards row
        JPanel metricsRow = new JPanel(new GridLayout(1, 6, 12, 0));
        metricsRow.setOpaque(false);
        metricsRow.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));

        packetsPerSec = new MetricCard("📡", "Packets / sec", UITheme.ACCENT);
        totalPacketsCard = new MetricCard("📊", "Total Captured", UITheme.ACCENT_PURPLE);
        threatCountCard = new MetricCard("🛡", "Threats", UITheme.DANGER);
        protocolCard = new MetricCard("🔗", "Protocols", UITheme.SUCCESS);
        captureStatusCard = new MetricCard("⏺", "Capture Status", UITheme.WARNING);
        captureStatusCard.setValueText("Idle");
        networkCard = new MetricCard("🌐", "Interface", UITheme.PROTO_TCP);
        networkCard.setValueText("—");

        metricsRow.add(packetsPerSec);
        metricsRow.add(totalPacketsCard);
        metricsRow.add(threatCountCard);
        metricsRow.add(protocolCard);
        metricsRow.add(captureStatusCard);
        metricsRow.add(networkCard);

        // Charts
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        chartsPanel.setOpaque(false);

        // Line chart
        XYSeriesCollection lineDS = new XYSeriesCollection(packetsSeries);
        JFreeChart lineChart = ChartFactory.createXYLineChart(
                null, "Time (s)", "Packets", lineDS,
                PlotOrientation.VERTICAL, false, true, false);
        styleChart(lineChart);
        ChartPanel linePanel = new ChartPanel(lineChart);
        linePanel.setMinimumDrawWidth(0); linePanel.setMinimumDrawHeight(0);
        GlassCard lineCard = new GlassCard(UITheme.ACCENT);
        lineCard.setLayout(new BorderLayout());
        JLabel lineLbl = new JLabel("  📈 Live Traffic Rate");
        lineLbl.setFont(UITheme.FONT_BODY_BOLD);
        lineLbl.setForeground(UITheme.ACCENT);
        lineLbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        lineCard.add(lineLbl, BorderLayout.NORTH);
        lineCard.add(linePanel, BorderLayout.CENTER);
        chartsPanel.add(lineCard);

        // Pie chart
        // JFreeChart pieChart = ChartFactory.createPieChart(null, protocolDataset, true, true, false);
        // styleChart(pieChart);
        // if (pieChart.getPlot() instanceof PiePlot) {
        //     PiePlot pp = (PiePlot) pieChart.getPlot();
        //     pp.setBackgroundPaint(UITheme.BG_SECONDARY);
        //     pp.setOutlinePaint(UITheme.BORDER_COLOR);
        //     pp.setLabelPaint(UITheme.TEXT_PRIMARY);
        //     pp.setShadowPaint(null);
        //     pp.setLabelFont(UITheme.FONT_SMALL);
        //     pp.setSectionPaint("TCP", UITheme.PROTO_TCP);
        //     pp.setSectionPaint("UDP", UITheme.PROTO_UDP);
        //     pp.setSectionPaint("ICMP", UITheme.PROTO_ICMP);
        //     pp.setSectionPaint("ARP", UITheme.PROTO_ARP);
        //     pp.setSectionPaint("Other", UITheme.PROTO_OTHER);
        // }
        JFreeChart pieChart = ChartFactory.createPieChart(
        null,
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

    // Better readable labels
    pp.setLabelPaint(Color.WHITE);
    pp.setLabelBackgroundPaint(new Color(30, 30, 30));
    pp.setLabelOutlinePaint(Color.DARK_GRAY);
    pp.setLabelShadowPaint(null);

    // Remove pie shadow
    pp.setShadowPaint(null);

    // Font
    pp.setLabelFont(UITheme.FONT_SMALL);

    // Cleaner labels
    pp.setSimpleLabels(true);

    // Protocol colors
    pp.setSectionPaint("TCP", UITheme.PROTO_TCP);
    pp.setSectionPaint("UDP", UITheme.PROTO_UDP);
    pp.setSectionPaint("ICMP", UITheme.PROTO_ICMP);
    pp.setSectionPaint("ARP", UITheme.PROTO_ARP);
    pp.setSectionPaint("Other", UITheme.PROTO_OTHER);
}
        ChartPanel piePanel = new ChartPanel(pieChart);
        piePanel.setMinimumDrawWidth(0); piePanel.setMinimumDrawHeight(0);
        GlassCard pieCard = new GlassCard(UITheme.ACCENT_PURPLE);
        pieCard.setLayout(new BorderLayout());
        JLabel pieLbl = new JLabel("  🔗 Protocol Distribution");
        pieLbl.setFont(UITheme.FONT_BODY_BOLD);
        pieLbl.setForeground(UITheme.ACCENT_PURPLE);
        pieLbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pieCard.add(pieLbl, BorderLayout.NORTH);
        pieCard.add(piePanel, BorderLayout.CENTER);
        chartsPanel.add(pieCard);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(metricsRow, BorderLayout.NORTH);
        centerPanel.add(chartsPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
        if (chart.getTitle() != null) {
            chart.getTitle().setPaint(UITheme.ACCENT);
            chart.getTitle().setFont(UITheme.FONT_BODY_BOLD);
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(UITheme.BG_SECONDARY);
            chart.getLegend().setItemPaint(UITheme.TEXT_SECONDARY);
            chart.getLegend().setItemFont(UITheme.FONT_SMALL);
        }
        if (chart.getPlot() instanceof XYPlot) {
            XYPlot plot = chart.getXYPlot();
            plot.setBackgroundPaint(UITheme.BG_PRIMARY);
            plot.setDomainGridlinePaint(new Color(30, 41, 59));
            plot.setRangeGridlinePaint(new Color(30, 41, 59));
            plot.setOutlinePaint(UITheme.BORDER_COLOR);
            plot.getRenderer().setSeriesPaint(0, UITheme.ACCENT);
            if (plot.getRenderer() instanceof org.jfree.chart.renderer.xy.XYLineAndShapeRenderer) {
                ((org.jfree.chart.renderer.xy.XYLineAndShapeRenderer) plot.getRenderer())
                        .setSeriesStroke(0, new BasicStroke(2f));
            }
            plot.getDomainAxis().setTickLabelPaint(UITheme.TEXT_MUTED);
            plot.getDomainAxis().setLabelPaint(UITheme.TEXT_SECONDARY);
            plot.getDomainAxis().setTickLabelFont(UITheme.FONT_TINY);
            plot.getRangeAxis().setTickLabelPaint(UITheme.TEXT_MUTED);
            plot.getRangeAxis().setLabelPaint(UITheme.TEXT_SECONDARY);
            plot.getRangeAxis().setTickLabelFont(UITheme.FONT_TINY);
        }
    }

    public void updateMetrics(int pps, int total, int threats, int protocols) {
        packetsPerSec.setValue(pps);
        totalPacketsCard.setValue(total);
        threatCountCard.setValue(threats);
        protocolCard.setValue(protocols);
    }

    public void setCaptureStatus(String status) { captureStatusCard.setValueText(status); }
    public void setNetworkInterface(String iface) { networkCard.setValueText(iface); }
    public void incrementThreats() { totalThreats++; threatCountCard.setValue(totalThreats); }
    public int getTotalThreats() { return totalThreats; }
}
