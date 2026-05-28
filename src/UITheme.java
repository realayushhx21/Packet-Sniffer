package com.first;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class UITheme {

    // ─── SOC Cyber Color Palette ───
    public static final Color BG_PRIMARY      = new Color(11, 16, 32);
    public static final Color BG_SECONDARY    = new Color(17, 24, 39);
    public static final Color BG_TERTIARY     = new Color(30, 41, 59);
    public static final Color BG_CARD         = new Color(30, 41, 59, 200);
    public static final Color BG_GLASS        = new Color(30, 41, 59, 140);
    public static final Color BG_SIDEBAR      = new Color(13, 18, 36);
    public static final Color BG_TOPBAR       = new Color(15, 20, 38);

    public static final Color ACCENT          = new Color(0, 229, 255);
    public static final Color ACCENT_DARK     = new Color(0, 180, 200);
    public static final Color ACCENT_LIGHT    = new Color(100, 240, 255);
    public static final Color ACCENT_GLOW     = new Color(0, 229, 255, 60);
    public static final Color ACCENT_PURPLE   = new Color(139, 92, 246);
    public static final Color ACCENT_PURPLE_GLOW = new Color(139, 92, 246, 50);

    public static final Color SUCCESS         = new Color(0, 255, 179);
    public static final Color SUCCESS_DARK    = new Color(0, 200, 140);
    public static final Color DANGER          = new Color(255, 77, 77);
    public static final Color DANGER_DARK     = new Color(200, 50, 50);
    public static final Color DANGER_GLOW     = new Color(255, 77, 77, 50);
    public static final Color WARNING         = new Color(255, 176, 32);
    public static final Color WARNING_DARK    = new Color(200, 140, 20);

    public static final Color TEXT_PRIMARY    = new Color(230, 237, 250);
    public static final Color TEXT_SECONDARY  = new Color(148, 163, 184);
    public static final Color TEXT_MUTED      = new Color(100, 116, 139);
    public static final Color BORDER_COLOR    = new Color(51, 65, 85);
    public static final Color BORDER_GLOW     = new Color(0, 229, 255, 40);

    public static final Color ROW_EVEN        = new Color(15, 23, 42);
    public static final Color ROW_ODD         = new Color(20, 28, 48);
    public static final Color ROW_SELECTED    = new Color(0, 120, 160, 100);
    public static final Color ROW_THREAT      = new Color(255, 50, 50, 30);

    public static final Color TERMINAL_GREEN  = new Color(0, 255, 179);
    public static final Color TERMINAL_BG     = new Color(8, 12, 21);

    public static final Color GRADIENT_START  = new Color(11, 16, 32);
    public static final Color GRADIENT_END    = new Color(0, 50, 70);

    // Protocol colors
    public static final Color PROTO_TCP   = new Color(59, 130, 246);
    public static final Color PROTO_UDP   = new Color(139, 92, 246);
    public static final Color PROTO_HTTP  = new Color(34, 197, 94);
    public static final Color PROTO_HTTPS = new Color(0, 229, 255);
    public static final Color PROTO_DNS   = new Color(250, 204, 21);
    public static final Color PROTO_ICMP  = new Color(251, 146, 60);
    public static final Color PROTO_ARP   = new Color(244, 114, 182);
    public static final Color PROTO_OTHER = new Color(100, 116, 139);

    // ─── Fonts ───
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE  = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TINY      = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font FONT_MONO      = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_MONO_LG   = new Font("Consolas", Font.PLAIN, 14);
    public static final Font FONT_MONO_SM   = new Font("Consolas", Font.PLAIN, 11);
    public static final Font FONT_BUTTON    = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_BIG_VALUE = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font FONT_METRIC    = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SIDEBAR   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER    = new Font("Segoe UI", Font.BOLD, 16);

    // ─── Dimensions ───
    public static final int PADDING = 16;
    public static final int PADDING_LARGE = 24;
    public static final int BUTTON_ARC = 10;
    public static final int CARD_ARC = 14;
    public static final int SIDEBAR_WIDTH = 220;
    public static final int SIDEBAR_COLLAPSED = 60;
    public static final int TOPBAR_HEIGHT = 50;
    public static final int STATUSBAR_HEIGHT = 32;
    public static final Dimension BUTTON_SIZE = new Dimension(180, 38);

    public static void initTheme() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatDarkLaf");
        }
        UIManager.put("Button.arc", BUTTON_ARC);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Button.innerFocusWidth", 0);
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("Component.borderColor", BORDER_COLOR);
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("List.selectionBackground", ROW_SELECTED);
        UIManager.put("List.selectionForeground", TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground", ROW_SELECTED);
        UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.gridColor", BORDER_COLOR);
        UIManager.put("TextField.background", BG_TERTIARY);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.background", BG_TERTIARY);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("ProgressBar.foreground", ACCENT);
        UIManager.put("TabbedPane.selectedBackground", BG_TERTIARY);
        UIManager.put("TabbedPane.focusColor", ACCENT);
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("OptionPane.background", BG_SECONDARY);
        UIManager.put("Panel.background", BG_PRIMARY);
    }

    // ─── Protocol color helper ───
    public static Color getProtocolColor(String protocol) {
        if (protocol == null) return PROTO_OTHER;
        switch (protocol.toUpperCase()) {
            case "TCP":   return PROTO_TCP;
            case "UDP":   return PROTO_UDP;
            case "HTTP":  return PROTO_HTTP;
            case "HTTPS": return PROTO_HTTPS;
            case "DNS":   return PROTO_DNS;
            case "ICMP":  return PROTO_ICMP;
            case "ARP":   return PROTO_ARP;
            default:      return PROTO_OTHER;
        }
    }

    // ─── Gradient header panel ───
    public static JPanel createGradientHeader(String title) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, GRADIENT_START, getWidth(), getHeight(), GRADIENT_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT_DARK),
                BorderFactory.createEmptyBorder(16, 20, 12, 20)));
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(ACCENT_LIGHT);
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    public static JPanel createGradientHeader(String title, JComponent sub) {
        JPanel panel = createGradientHeader(title);
        if (sub != null) {
            sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            panel.add(sub, BorderLayout.SOUTH);
        }
        return panel;
    }

    // ─── Cyber button with glow hover ───
    public static JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            private float glowAlpha = 0f;
            private Timer glowTimer;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { animateGlow(true); }
                    @Override public void mouseExited(MouseEvent e) { animateGlow(false); }
                });
            }
            private void animateGlow(boolean in) {
                if (glowTimer != null) glowTimer.stop();
                glowTimer = new Timer(16, evt -> {
                    glowAlpha += in ? 0.08f : -0.08f;
                    glowAlpha = Math.max(0f, Math.min(1f, glowAlpha));
                    repaint();
                    if ((in && glowAlpha >= 1f) || (!in && glowAlpha <= 0f)) ((Timer)evt.getSource()).stop();
                });
                glowTimer.start();
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                // Shadow/glow
                if (glowAlpha > 0) {
                    Color glowColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(40 * glowAlpha));
                    g2.setColor(glowColor);
                    g2.fill(new RoundRectangle2D.Float(-3, -3, w + 6, h + 6, BUTTON_ARC + 4, BUTTON_ARC + 4));
                }
                Color bg = getModel().isPressed() ? bgColor.darker().darker() :
                           glowAlpha > 0 ? brighter(bgColor, 0.15f * glowAlpha) : bgColor;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, BUTTON_ARC, BUTTON_ARC));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setPreferredSize(BUTTON_SIZE);
        return btn;
    }

    private static Color brighter(Color c, float factor) {
        int r = Math.min(255, (int)(c.getRed() + (255 - c.getRed()) * factor));
        int g = Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * factor));
        int b = Math.min(255, (int)(c.getBlue() + (255 - c.getBlue()) * factor));
        return new Color(r, g, b);
    }

    // ─── Protocol badge label ───
    public static JLabel createProtocolBadge(String protocol) {
        Color c = getProtocolColor(protocol);
        JLabel badge = new JLabel(protocol, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 80));
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_TINY);
        badge.setForeground(c);
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        badge.setPreferredSize(new Dimension(60, 22));
        return badge;
    }

    // ─── Styled table with protocol colors ───
    public static JTable createStyledTable(TableModel model) {
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(ROW_SELECTED);
                    c.setForeground(ACCENT_LIGHT);
                } else {
                    c.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                    c.setForeground(TEXT_PRIMARY);
                }
                if (c instanceof JComponent)
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return c;
            }
        };
        table.setFont(FONT_MONO);
        table.setRowHeight(34);
        table.setGridColor(new Color(30, 41, 59));
        table.setBackground(BG_PRIMARY);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(ACCENT_LIGHT);
        table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(BG_TERTIARY);
        header.setForeground(ACCENT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_DARK));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setBackground(BG_TERTIARY);
                lbl.setForeground(ACCENT);
                lbl.setFont(FONT_BODY_BOLD);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_DARK),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)));
                return lbl;
            }
        });
        return table;
    }

    public static <T> JList<T> createStyledList(ListModel<T> model) {
        JList<T> list = new JList<>(model);
        list.setFont(FONT_MONO);
        list.setBackground(BG_SECONDARY);
        list.setForeground(TEXT_PRIMARY);
        list.setSelectionBackground(ROW_SELECTED);
        list.setSelectionForeground(Color.WHITE);
        list.setFixedCellHeight(32);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int idx, boolean sel, boolean foc) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(l, v, idx, sel, foc);
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                if (sel) { lbl.setBackground(ROW_SELECTED); lbl.setForeground(ACCENT_LIGHT); }
                else { lbl.setBackground(idx % 2 == 0 ? ROW_EVEN : ROW_ODD); lbl.setForeground(TEXT_PRIMARY); }
                return lbl;
            }
        });
        return list;
    }

    public static TitledBorder createSectionBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                " " + title + " ", TitledBorder.LEFT, TitledBorder.TOP,
                FONT_BODY_BOLD, ACCENT);
    }

    public static JPanel createTitledPanel(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
                createSectionBorder(title),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    public static JTextArea createTerminalTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(FONT_MONO_LG);
        area.setBackground(TERMINAL_BG);
        area.setForeground(TERMINAL_GREEN);
        area.setCaretColor(TERMINAL_GREEN);
        area.setSelectionColor(new Color(0, 100, 60));
        area.setSelectedTextColor(Color.WHITE);
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return area;
    }

    public static JScrollPane createStyledScrollPane(JComponent view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getViewport().setBackground(BG_SECONDARY);
        return sp;
    }

    public static void stylePanel(JPanel panel) {
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
    }

    public static JProgressBar createStyledProgressBar(String text) {
        JProgressBar bar = new JProgressBar();
        bar.setStringPainted(true);
        bar.setString(text);
        bar.setFont(FONT_BODY);
        bar.setForeground(ACCENT);
        bar.setBackground(BG_TERTIARY);
        bar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return bar;
    }

    // ─── Glass panel factory ───
    public static JPanel createGlassPanel() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_GLASS);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC));
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, CARD_ARC, CARD_ARC));
                g2.dispose();
            }
        };
    }

    // ─── Search field ───
    public static JTextField createSearchField(String placeholder) {
        JTextField field = new JTextField(20) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(FONT_BODY);
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left + 4, getHeight() / 2 + 4);
                    g2.dispose();
                }
            }
        };
        field.setFont(FONT_MONO);
        field.setBackground(BG_TERTIARY);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return field;
    }

    // ─── Fade-in animation ───
    public static void fadeIn(JComponent comp, int durationMs) {
        comp.setVisible(true);
        if (comp instanceof JPanel) {
            float[] alpha = {0f};
            Timer timer = new Timer(16, null);
            float step = 16f / durationMs;
            timer.addActionListener(e -> {
                alpha[0] = Math.min(1f, alpha[0] + step);
                comp.repaint();
                if (alpha[0] >= 1f) timer.stop();
            });
            timer.start();
        }
    }

    // ─── Animated counter helper ───
    public static void animateCounter(JLabel label, int from, int to, int durationMs) {
        int steps = Math.max(1, durationMs / 16);
        Timer timer = new Timer(16, null);
        int[] step = {0};
        timer.addActionListener(e -> {
            step[0]++;
            float t = Math.min(1f, (float) step[0] / steps);
            t = t * t * (3 - 2 * t); // smoothstep
            int val = (int)(from + (to - from) * t);
            label.setText(String.valueOf(val));
            if (step[0] >= steps) { label.setText(String.valueOf(to)); ((Timer)e.getSource()).stop(); }
        });
        timer.start();
    }
}
