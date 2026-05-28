package com.first.components;

import com.first.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class SidebarNav extends JPanel {
    private final List<NavItem> items = new ArrayList<>();
    private int selectedIndex = 0;
    private boolean collapsed = false;
    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final JPanel itemsPanel;

    public SidebarNav(CardLayout contentLayout, JPanel contentPanel) {
        this.contentLayout = contentLayout;
        this.contentPanel = contentPanel;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SIDEBAR);
        setPreferredSize(new Dimension(UITheme.SIDEBAR_WIDTH, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR));

        // Brand header
        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(16, 16, 20, 16));
        JLabel logo = new JLabel("⚡ Packet Sniffer");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(UITheme.ACCENT);
        brand.add(logo, BorderLayout.WEST);

        JButton collapseBtn = new JButton("◀");
        collapseBtn.setFont(UITheme.FONT_SMALL);
        collapseBtn.setForeground(UITheme.TEXT_MUTED);
        collapseBtn.setBackground(UITheme.BG_SIDEBAR);
        collapseBtn.setBorderPainted(false);
        collapseBtn.setFocusPainted(false);
        collapseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        collapseBtn.addActionListener(e -> toggleCollapse(logo, collapseBtn));
        brand.add(collapseBtn, BorderLayout.EAST);
        add(brand, BorderLayout.NORTH);

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(itemsPanel, BorderLayout.CENTER);

        // Bottom status
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        JLabel status = new JLabel("● System Online");
        status.setFont(UITheme.FONT_SMALL);
        status.setForeground(UITheme.SUCCESS);
        bottom.add(status, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);
    }

    private void toggleCollapse(JLabel logo, JButton btn) {
        collapsed = !collapsed;
        logo.setText(collapsed ? "⚡" : "⚡ Packet Sniffer");
        btn.setText(collapsed ? "▶" : "◀");
        setPreferredSize(new Dimension(collapsed ? UITheme.SIDEBAR_COLLAPSED : UITheme.SIDEBAR_WIDTH, 0));
        for (NavItem item : items) item.setCollapsed(collapsed);
        revalidate(); repaint();
        getParent().revalidate();
    }

    public void addNavItem(String icon, String label, String cardName) {
        int idx = items.size();
        NavItem item = new NavItem(icon, label, idx);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectItem(idx, cardName); }
        });
        items.add(item);
        itemsPanel.add(item);
        itemsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        if (idx == 0) item.setSelected(true);
    }

    public void selectItem(int idx, String cardName) {
        for (int i = 0; i < items.size(); i++) items.get(i).setSelected(i == idx);
        selectedIndex = idx;
        contentLayout.show(contentPanel, cardName);
    }

    public void selectByName(String cardName) {
        contentLayout.show(contentPanel, cardName);
    }

    private class NavItem extends JPanel {
        private boolean selected = false;
        private boolean hovered = false;
        private final JLabel iconLbl, textLbl;
        private float glowAlpha = 0f;

        NavItem(String icon, String text, int idx) {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setPreferredSize(new Dimension(200, 40));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            iconLbl = new JLabel(icon);
            iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            iconLbl.setForeground(UITheme.TEXT_MUTED);
            add(iconLbl);

            textLbl = new JLabel(text);
            textLbl.setFont(UITheme.FONT_SIDEBAR);
            textLbl.setForeground(UITheme.TEXT_SECONDARY);
            add(textLbl);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        void setSelected(boolean sel) {
            this.selected = sel;
            iconLbl.setForeground(sel ? UITheme.ACCENT : UITheme.TEXT_MUTED);
            textLbl.setForeground(sel ? UITheme.ACCENT : UITheme.TEXT_SECONDARY);
            textLbl.setFont(sel ? UITheme.FONT_BODY_BOLD : UITheme.FONT_SIDEBAR);
            repaint();
        }

        void setCollapsed(boolean c) { textLbl.setVisible(!c); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            if (selected) {
                g2.setColor(new Color(0, 229, 255, 15));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 8, 8));
                g2.setColor(UITheme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 4, 3, h - 8, 2, 2));
            } else if (hovered) {
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 8, 8));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
