package com.first.panels;

import com.first.*;
import com.first.components.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ThreatPanel extends JPanel {
    private final DefaultTableModel alertTableModel;
    private final JLabel totalAlertsLabel;
    private final JTextArea liveAlertFeed;
    private final JTextArea detailArea;
    private int alertCount = 0;
    private final List<String[]> alertDetails = new ArrayList<>();

    public ThreatPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = new JLabel("🛡 Threat Detection Center");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.DANGER);
        titleRow.add(title, BorderLayout.WEST);
        totalAlertsLabel = new JLabel("0 alerts detected");
        totalAlertsLabel.setFont(UITheme.FONT_BODY);
        totalAlertsLabel.setForeground(UITheme.TEXT_MUTED);
        titleRow.add(totalAlertsLabel, BorderLayout.EAST);
        add(titleRow, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        centerPanel.setOpaque(false);

        // Alerts table
        String[] cols = {"#", "Time", "Severity", "Message"};
        alertTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable alertTable = UITheme.createStyledTable(alertTableModel);
        alertTable.getColumnModel().getColumn(0).setMaxWidth(40);
        alertTable.getColumnModel().getColumn(1).setMaxWidth(100);
        alertTable.getColumnModel().getColumn(2).setMaxWidth(80);
        alertTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                String sev = v != null ? v.toString() : "";
                lbl.setForeground(sev.equals("HIGH") ? UITheme.DANGER : UITheme.WARNING);
                lbl.setFont(UITheme.FONT_BODY_BOLD);
                lbl.setHorizontalAlignment(CENTER);
                if (!s) lbl.setBackground(r % 2 == 0 ? UITheme.ROW_EVEN : UITheme.ROW_ODD);
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return lbl;
            }
        });

        // Detail area (init before listener references it)
        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(UITheme.FONT_MONO);
        detailArea.setBackground(new Color(10, 16, 28));
        detailArea.setForeground(UITheme.ACCENT_LIGHT);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        detailArea.setText("Select a threat from the table above to view its\nmathematical detection logic and remediation steps.\n");

        alertTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = alertTable.getSelectedRow();
                if (row >= 0 && row < alertDetails.size()) {
                    String[] d = alertDetails.get(row);
                    detailArea.setText(d[1] + "\n" + "~".repeat(68) + "\n\n" + d[2]);
                    detailArea.setCaretPosition(0);
                }
            }
        });

        GlassCard tableCard = new GlassCard(UITheme.DANGER);
        tableCard.setLayout(new BorderLayout());
        JLabel tblLbl = new JLabel("  ⚠ Alert History — Click row for details");
        tblLbl.setFont(UITheme.FONT_BODY_BOLD);
        tblLbl.setForeground(UITheme.DANGER);
        tblLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        tableCard.add(tblLbl, BorderLayout.NORTH);
        tableCard.add(UITheme.createStyledScrollPane(alertTable), BorderLayout.CENTER);
        centerPanel.add(tableCard);

        GlassCard detailCard = new GlassCard(UITheme.ACCENT);
        detailCard.setLayout(new BorderLayout());
        JLabel detailLbl = new JLabel("  📊 Mathematical Detection Logic & Remediation");
        detailLbl.setFont(UITheme.FONT_BODY_BOLD);
        detailLbl.setForeground(UITheme.ACCENT);
        detailLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        detailCard.add(detailLbl, BorderLayout.NORTH);
        detailCard.add(UITheme.createStyledScrollPane(detailArea), BorderLayout.CENTER);
        centerPanel.add(detailCard);

        // Live feed
        liveAlertFeed = new JTextArea();
        liveAlertFeed.setEditable(false);
        liveAlertFeed.setFont(UITheme.FONT_MONO);
        liveAlertFeed.setBackground(new Color(20, 10, 10));
        liveAlertFeed.setForeground(UITheme.DANGER);
        liveAlertFeed.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        GlassCard feedCard = new GlassCard(UITheme.WARNING);
        feedCard.setLayout(new BorderLayout());
        JLabel feedLbl = new JLabel("  🔴 Live Alert Stream");
        feedLbl.setFont(UITheme.FONT_BODY_BOLD);
        feedLbl.setForeground(UITheme.WARNING);
        feedLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        feedCard.add(feedLbl, BorderLayout.NORTH);
        feedCard.add(UITheme.createStyledScrollPane(liveAlertFeed), BorderLayout.CENTER);
        centerPanel.add(feedCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void addAlert(String timestamp, String severity, String message) {
        alertCount++;
        alertTableModel.insertRow(0, new Object[]{alertCount, timestamp, severity, message});
        totalAlertsLabel.setText(alertCount + " alerts detected");
        totalAlertsLabel.setForeground(UITheme.DANGER);
        liveAlertFeed.append("[" + timestamp + "] [" + severity + "] " + message + "\n");
        liveAlertFeed.setCaretPosition(liveAlertFeed.getDocument().getLength());
        String type = detectType(message);
        alertDetails.add(0, new String[]{type, buildMath(type, message), buildFix(type)});
    }

    private String detectType(String m) {
        if (m.contains("PORT SCAN")) return "PORT_SCAN";
        if (m.contains("ARP SPOOFING")) return "ARP_SPOOF";
        if (m.contains("ARP FLOOD")) return "ARP_FLOOD";
        if (m.contains("SYN FLOOD")) return "SYN_FLOOD";
        if (m.contains("DNS AMPLIFICATION")) return "DNS_AMP";
        if (m.contains("ICMP FLOOD")) return "ICMP_FLOOD";
        if (m.contains("SUSPICIOUS PORT")) return "SUS_PORT";
        if (m.contains("LARGE PACKET")) return "LARGE_PKT";
        if (m.contains("TTL ANOMALY")) return "TTL_ANOM";
        if (m.contains("FIN SCAN")) return "FIN_SCAN";
        if (m.contains("NULL SCAN")) return "NULL_SCAN";
        if (m.contains("DoS")) return "DOS";
        return "UNKNOWN";
    }

    private String num(String m, String pre, String suf) {
        try { int s=m.indexOf(pre); if(s<0) return "0"; s+=pre.length(); int e=m.indexOf(suf,s); return e<0?"0":m.substring(s,e).trim(); } catch(Exception e){return "0";}
    }
    private int pint(String s, int d) { try{return Integer.parseInt(s.trim());}catch(Exception e){return d;} }

    private String buildMath(String type, String m) {
        int pst = ConfigManager.getPortScanThreshold(), psw = ConfigManager.getPortScanWindowSeconds();
        int dt = ConfigManager.getDosThreshold(), dw = ConfigManager.getDosWindowSeconds();
        StringBuilder s = new StringBuilder();
        switch(type) {
            case "PORT_SCAN": {
                int ports=pint(num(m,"("," ports"),0), w=pint(num(m,"in ","s)"),psw);
                s.append("=== PORT SCAN DETECTION ===\n\n");
                s.append("Algorithm: SYN Packet Unique Port Counting (Sliding Window)\n\n");
                s.append("For each SYN-only packet (SYN=1, ACK=0):\n");
                s.append("  1. Record [timestamp, destPort] per source IP\n");
                s.append("  2. Remove entries older than W seconds\n");
                s.append("  3. Count unique destination ports\n\n");
                s.append("Formula: UniqueDestPorts(srcIP, W) > T\n");
                s.append("  W = ").append(w).append("s, T = ").append(pst).append(" ports\n\n");
                s.append("Observed: ").append(ports).append(" unique ports > ").append(pst).append(" => TRIGGERED\n");
                s.append("Scan Rate: ").append(String.format("%.2f", (double)ports/Math.max(1,w))).append(" ports/sec\n");
                break;
            }
            case "ARP_SPOOF": {
                int mi=m.indexOf("(MACs: "); String ms=mi>=0?m.substring(mi+7,m.indexOf(")",mi)):"?";
                String[] macs=ms.split(",");
                s.append("=== ARP SPOOFING DETECTION ===\n\n");
                s.append("Algorithm: IP-to-MAC Consistency Check\n\n");
                s.append("For each ARP reply: maintain mapping IP -> Set<MAC>\n");
                s.append("Formula: |MAC_Set(IP)| > 1\n\n");
                s.append("Observed: ").append(macs.length).append(" MACs for same IP => TRIGGERED\n");
                for(int i=0;i<macs.length;i++) s.append("  MAC_").append(i+1).append(" = ").append(macs[i].trim()).append("\n");
                s.append("Confidence: ").append(String.format("%.0f%%",(1.0-1.0/macs.length)*100)).append("\n");
                break;
            }
            case "ARP_FLOOD": {
                int pk=pint(num(m,", "," ARP"),0), w=pint(num(m,"in ","s)"),10);
                s.append("=== ARP FLOOD DETECTION ===\n\n");
                s.append("Algorithm: ARP Rate Analysis per Source MAC (Sliding Window)\n\n");
                s.append("Formula: ARPCount(srcMAC, W) > T\n");
                s.append("  W = ").append(w).append("s, T = 30 packets\n\n");
                s.append("Observed: ").append(pk).append(" ARP packets > 30 => TRIGGERED\n");
                s.append("Rate: ").append(String.format("%.1f",(double)pk/Math.max(1,w))).append(" ARP/sec\n");
                break;
            }
            case "DOS": {
                int pk=pint(num(m,"("," packets"),0), w=pint(num(m,"in ","s)"),dw);
                s.append("=== DoS DETECTION ===\n\n");
                s.append("Algorithm: High-Volume Traffic Rate (Sliding Window)\n\n");
                s.append("Formula: PacketCount(srcIP, W) > T\n");
                s.append("  W = ").append(w).append("s, T = ").append(dt).append(" packets\n\n");
                s.append("Observed: ").append(pk).append(" packets > ").append(dt).append(" => TRIGGERED\n");
                s.append("Rate: ").append(String.format("%.1f",(double)pk/Math.max(1,w))).append(" pkt/sec\n");
                s.append("Excess: ").append(String.format("%.2fx",(double)pk/Math.max(1,dt))).append(" above threshold\n");
                break;
            }
            case "SYN_FLOOD": {
                int pk=pint(num(m,"("," SYN"),0), w=pint(num(m,"in ","s)"),5);
                s.append("=== SYN FLOOD DETECTION ===\n\n");
                s.append("Algorithm: SYN Packet Rate per src->dst pair (Sliding Window)\n\n");
                s.append("Formula: SYNCount(srcIP->dstIP, W) > T\n");
                s.append("  W = ").append(w).append("s, T = 50 SYN packets\n\n");
                s.append("Observed: ").append(pk).append(" SYN packets > 50 => TRIGGERED\n");
                s.append("Rate: ").append(String.format("%.1f",(double)pk/Math.max(1,w))).append(" SYN/sec\n");
                s.append("\nSYN Flood exhausts server's TCP backlog queue by\n");
                s.append("sending SYN packets without completing 3-way handshake.\n");
                break;
            }
            case "DNS_AMP": {
                int pk=pint(num(m,"("," DNS"),0), w=pint(num(m,"in ","s,"),5);
                s.append("=== DNS AMPLIFICATION DETECTION ===\n\n");
                s.append("Algorithm: DNS (port 53) Packet Rate (Sliding Window)\n\n");
                s.append("Formula: DNSCount(srcIP, W) > T\n");
                s.append("  W = ").append(w).append("s, T = 30 DNS packets\n\n");
                s.append("Observed: ").append(pk).append(" DNS packets > 30 => TRIGGERED\n");
                s.append("\nDNS Amplification uses open DNS resolvers to amplify\n");
                s.append("small queries into large responses directed at the victim.\n");
                s.append("Amplification Factor can reach 28x-54x.\n");
                break;
            }
            case "ICMP_FLOOD": {
                int pk=pint(num(m,"("," ICMP"),0), w=pint(num(m,"in ","s)"),5);
                s.append("=== ICMP FLOOD DETECTION ===\n\n");
                s.append("Algorithm: ICMP Packet Rate per Source (Sliding Window)\n\n");
                s.append("Formula: ICMPCount(srcIP, W) > T\n");
                s.append("  W = ").append(w).append("s, T = 40 ICMP packets\n\n");
                s.append("Observed: ").append(pk).append(" ICMP packets > 40 => TRIGGERED\n");
                s.append("Rate: ").append(String.format("%.1f",(double)pk/Math.max(1,w))).append(" ICMP/sec\n");
                s.append("\nICMP Flood (Ping Flood) overwhelms target with\n");
                s.append("Echo Request packets, consuming bandwidth.\n");
                break;
            }
            case "SUS_PORT": {
                s.append("=== SUSPICIOUS PORT DETECTION ===\n\n");
                s.append("Algorithm: Known Malicious Port Matching\n\n");
                s.append("Formula: dstPort IN {4444, 5555, 6666, 6667, 31337,\n");
                s.append("         1234, 12345, 27374, 20000, 65535}\n\n");
                s.append("Port Database:\n");
                s.append("  4444  = Metasploit default listener\n");
                s.append("  5555  = Android ADB remote\n");
                s.append("  6666  = IRC backdoor\n");
                s.append("  6667  = IRC C&C channel\n");
                s.append("  31337 = Back Orifice trojan\n");
                s.append("  12345 = NetBus trojan\n");
                s.append("  27374 = SubSeven trojan\n\n");
                s.append("Result: Port matched => TRIGGERED\n");
                break;
            }
            case "LARGE_PKT": {
                int sz=pint(num(m,"("," bytes"),0);
                s.append("=== LARGE PACKET DETECTION ===\n\n");
                s.append("Algorithm: Packet Size Threshold Check\n\n");
                s.append("Formula: PacketSize > T (T = 1400 bytes)\n\n");
                s.append("Observed: ").append(sz).append(" bytes > 1400 => TRIGGERED\n\n");
                s.append("Large packets may indicate:\n");
                s.append("  - Data exfiltration attempts\n");
                s.append("  - Buffer overflow exploits\n");
                s.append("  - Tunneling protocols\n");
                s.append("  - Fragmentation attacks\n");
                break;
            }
            case "TTL_ANOM": {
                String ttl = num(m, "TTL=", ",");
                s.append("=== TTL ANOMALY DETECTION ===\n\n");
                s.append("Algorithm: Time-To-Live Threshold Check\n\n");
                s.append("Formula: 0 < TTL <= 5\n\n");
                s.append("Observed TTL: ").append(ttl).append(" <= 5 => TRIGGERED\n\n");
                s.append("Low TTL indicates:\n");
                s.append("  - Traceroute reconnaissance\n");
                s.append("  - IDS/Firewall evasion techniques\n");
                s.append("  - Packet crafting tools (hping3, scapy)\n");
                s.append("  - Packets near end-of-life (routing loops)\n");
                break;
            }
            case "FIN_SCAN": {
                int ports=pint(num(m,"("," ports"),0), w=pint(num(m,"in ","s)"),10);
                s.append("=== FIN SCAN DETECTION ===\n\n");
                s.append("Algorithm: FIN-only Packet Unique Port Counting\n\n");
                s.append("FIN scan sends TCP packets with only FIN flag set.\n");
                s.append("Closed ports respond with RST; open ports stay silent.\n\n");
                s.append("Formula: UniqueFINPorts(srcIP, W) > T\n");
                s.append("  W = ").append(w).append("s, T = 5 ports\n\n");
                s.append("Observed: ").append(ports).append(" ports > 5 => TRIGGERED\n");
                s.append("This is a stealth scan technique (Nmap -sF).\n");
                break;
            }
            case "NULL_SCAN": {
                int ports=pint(num(m,"("," ports"),0), w=pint(num(m,"in ","s)"),10);
                s.append("=== NULL SCAN DETECTION ===\n\n");
                s.append("Algorithm: Zero-flag TCP Packet Counting\n\n");
                s.append("NULL scan sends TCP packets with NO flags set.\n");
                s.append("Per RFC 793, closed ports respond RST; open stay silent.\n\n");
                s.append("Formula: UniqueNULLPorts(srcIP, W) > T\n");
                s.append("  W = ").append(w).append("s, T = 5 ports\n\n");
                s.append("Observed: ").append(ports).append(" ports > 5 => TRIGGERED\n");
                s.append("This is a stealth scan technique (Nmap -sN).\n");
                break;
            }
            default: s.append("=== UNKNOWN THREAT ===\nNo model available.\n");
        }
        return s.toString();
    }

    private String buildFix(String type) {
        return switch(type) {
            case "PORT_SCAN" -> "=== REMEDIATION: PORT SCAN ===\n\nIMMEDIATE:\n  1. Block source IP: iptables -A INPUT -s <IP> -j DROP\n  2. Close unnecessary ports\n  3. Enable SYN cookies: sysctl -w net.ipv4.tcp_syncookies=1\n\nLONG-TERM:\n  4. Deploy IPS (Snort/Suricata)\n  5. Rate-limit SYN packets per source IP\n  6. Use port knocking or SPA\n  7. Network segmentation with VLANs\n  8. Regular port audits: nmap -sS <IP>\n  9. Disable ICMP echo responses\n";
            case "ARP_SPOOF" -> "=== REMEDIATION: ARP SPOOFING ===\n\nIMMEDIATE:\n  1. Static ARP: arp -s <GW_IP> <GW_MAC>\n  2. Disconnect suspected device\n  3. Flush ARP cache: ip neigh flush all\n\nLONG-TERM:\n  4. Enable Dynamic ARP Inspection (DAI)\n  5. Use DHCP Snooping\n  6. Deploy 802.1X NAC\n  7. Use encrypted protocols (HTTPS/SSH)\n  8. Deploy ARPwatch monitoring\n  9. Implement MACsec (802.1AE)\n";
            case "ARP_FLOOD" -> "=== REMEDIATION: ARP FLOOD ===\n\nIMMEDIATE:\n  1. Identify flooding device by MAC\n  2. Rate-limit ARP: storm-control broadcast level 10\n  3. Disable port if severe\n\nLONG-TERM:\n  4. ARP rate-limiting: ip arp inspection limit rate 15\n  5. Storm control for broadcast traffic\n  6. DHCP Snooping + DAI\n  7. Segment broadcast domains with VLANs\n  8. Investigate for malware\n";
            case "DOS" -> "=== REMEDIATION: DoS ATTACK ===\n\nIMMEDIATE:\n  1. Rate-limit source: iptables -m limit --limit 50/sec\n  2. Block IP: iptables -A INPUT -s <IP> -j DROP\n  3. Enable SYN cookies\n  4. Increase TCP backlog: tcp_max_syn_backlog=4096\n\nLONG-TERM:\n  5. DDoS mitigation (Cloudflare/AWS Shield)\n  6. Load balancers\n  7. NetFlow anomaly detection\n  8. Geo-blocking\n  9. Anycast routing\n";
            case "SYN_FLOOD" -> "=== REMEDIATION: SYN FLOOD ===\n\nIMMEDIATE:\n  1. Enable SYN cookies: sysctl -w net.ipv4.tcp_syncookies=1\n  2. Reduce SYN-RECV timeout: tcp_synack_retries=1\n  3. Increase backlog: tcp_max_syn_backlog=8192\n  4. Block source IP at firewall\n\nLONG-TERM:\n  5. Deploy SYN proxy (hardware firewall)\n  6. Rate-limit SYN packets per source\n  7. Use DDoS mitigation services\n  8. Implement TCP Fast Open (TFO)\n  9. Monitor half-open connections\n";
            case "DNS_AMP" -> "=== REMEDIATION: DNS AMPLIFICATION ===\n\nIMMEDIATE:\n  1. Rate-limit DNS traffic at firewall\n  2. Block known malicious DNS resolvers\n  3. Implement BCP38 (ingress filtering)\n\nLONG-TERM:\n  4. Disable open DNS recursion\n  5. Implement Response Rate Limiting (RRL)\n  6. Deploy DNSSEC\n  7. Use Anycast DNS infrastructure\n  8. Monitor for spoofed source IPs\n  9. DDoS mitigation service\n";
            case "ICMP_FLOOD" -> "=== REMEDIATION: ICMP FLOOD ===\n\nIMMEDIATE:\n  1. Rate-limit ICMP: iptables -p icmp --icmp-type echo-request -m limit --limit 1/s\n  2. Block ICMP from source\n  3. Disable ICMP echo: sysctl -w net.ipv4.icmp_echo_ignore_all=1\n\nLONG-TERM:\n  4. Configure ICMP rate-limiting at router\n  5. Implement CAR (Committed Access Rate)\n  6. Deploy IPS with ICMP flood rules\n  7. Use uRPF (Unicast Reverse Path Forwarding)\n";
            case "SUS_PORT" -> "=== REMEDIATION: SUSPICIOUS PORT ===\n\nIMMEDIATE:\n  1. Block the suspicious port: iptables -A INPUT -p tcp --dport <PORT> -j DROP\n  2. Investigate source machine for malware\n  3. Check for unauthorized services\n\nLONG-TERM:\n  4. Deploy application-layer firewall\n  5. Whitelist allowed ports only\n  6. Regular vulnerability scanning\n  7. Endpoint Detection & Response (EDR)\n  8. Network segmentation\n  9. Monitor C&C indicators (IoCs)\n";
            case "LARGE_PKT" -> "=== REMEDIATION: LARGE PACKET ===\n\nIMMEDIATE:\n  1. Inspect packet content for data exfiltration\n  2. Rate-limit large packets at firewall\n  3. Block if confirmed malicious\n\nLONG-TERM:\n  4. Set MTU limits at network boundary\n  5. Deploy DLP (Data Loss Prevention)\n  6. Monitor for tunneling protocols\n  7. Enable deep packet inspection\n  8. Fragment reassembly monitoring\n";
            case "TTL_ANOM" -> "=== REMEDIATION: TTL ANOMALY ===\n\nIMMEDIATE:\n  1. Investigate source for reconnaissance tools\n  2. Block source if malicious intent confirmed\n  3. Log and correlate with other alerts\n\nLONG-TERM:\n  4. Configure minimum TTL filters at firewall\n  5. Deploy IPS with TTL-based rules\n  6. Normalize TTL values at network edge\n  7. Monitor for traceroute patterns\n  8. Check for packet crafting tools\n";
            case "FIN_SCAN" -> "=== REMEDIATION: FIN SCAN ===\n\nIMMEDIATE:\n  1. Block source IP at firewall\n  2. Enable stateful packet inspection\n  3. Drop packets with only FIN flag\n\nLONG-TERM:\n  4. Deploy stateful firewall (track TCP state)\n  5. Use IPS rules for stealth scans\n  6. iptables: -A INPUT -p tcp --tcp-flags ALL FIN -j DROP\n  7. Network segmentation\n  8. Regular security audits\n";
            case "NULL_SCAN" -> "=== REMEDIATION: NULL SCAN ===\n\nIMMEDIATE:\n  1. Block source IP at firewall\n  2. Drop packets with no TCP flags set\n  3. Enable stateful inspection\n\nLONG-TERM:\n  4. iptables: -A INPUT -p tcp --tcp-flags ALL NONE -j DROP\n  5. Deploy stateful firewall\n  6. IPS rules for zero-flag packets\n  7. Monitor for Nmap scan patterns\n  8. Harden OS TCP/IP stack\n";
            default -> "=== REMEDIATION ===\n\n  1. Investigate the alert and correlate with logs\n  2. Isolate suspicious source\n  3. Consult security team\n";
        };
    }
}
