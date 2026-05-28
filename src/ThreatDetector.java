package com.first;

import org.pcap4j.packet.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Feature 4: Intrusion Detection Alerts
 * Analyzes captured packets for multiple cyber threats:
 * Port Scan, ARP Spoofing, ARP Flood, DoS, SYN Flood,
 * DNS Amplification, ICMP Flood, Suspicious Port, Large Packet, TTL Anomaly
 */
public class ThreatDetector {

    public static final String SEVERITY_HIGH = "HIGH";
    public static final String SEVERITY_MEDIUM = "MEDIUM";

    public interface AlertListener {
        void onAlert(String timestamp, String severity, String message);
    }

    private final List<AlertListener> listeners = new CopyOnWriteArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

    // --- Port Scan ---
    private final Map<String, List<long[]>> synTracker = new ConcurrentHashMap<>();
    private final Set<String> portScanAlerted = ConcurrentHashMap.newKeySet();

    // --- ARP Spoofing ---
    private final Map<String, Set<String>> arpTable = new ConcurrentHashMap<>();
    private final Set<String> arpSpoofAlerted = ConcurrentHashMap.newKeySet();

    // --- DoS / High Volume ---
    private final Map<String, List<Long>> volumeTracker = new ConcurrentHashMap<>();
    private final Set<String> dosAlerted = ConcurrentHashMap.newKeySet();

    // --- ARP Flood ---
    private final Map<String, List<Long>> arpFloodTracker = new ConcurrentHashMap<>();
    private final Set<String> arpFloodAlerted = ConcurrentHashMap.newKeySet();

    // --- SYN Flood ---
    private final Map<String, List<Long>> synFloodTracker = new ConcurrentHashMap<>();
    private final Set<String> synFloodAlerted = ConcurrentHashMap.newKeySet();

    // --- DNS Amplification ---
    private final Map<String, List<Long>> dnsTracker = new ConcurrentHashMap<>();
    private final Set<String> dnsAlerted = ConcurrentHashMap.newKeySet();

    // --- ICMP Flood ---
    private final Map<String, List<Long>> icmpTracker = new ConcurrentHashMap<>();
    private final Set<String> icmpAlerted = ConcurrentHashMap.newKeySet();

    // --- Suspicious Ports ---
    private final Set<String> suspiciousPortAlerted = ConcurrentHashMap.newKeySet();

    // --- Large Packet ---
    private final Set<String> largePacketAlerted = ConcurrentHashMap.newKeySet();

    // --- TTL Anomaly ---
    private final Set<String> ttlAlerted = ConcurrentHashMap.newKeySet();

    // --- FIN Scan ---
    private final Map<String, List<long[]>> finTracker = new ConcurrentHashMap<>();
    private final Set<String> finScanAlerted = ConcurrentHashMap.newKeySet();

    // --- NULL Scan ---
    private final Map<String, List<long[]>> nullTracker = new ConcurrentHashMap<>();
    private final Set<String> nullScanAlerted = ConcurrentHashMap.newKeySet();

    // Thresholds
    private final int portScanThreshold;
    private final int portScanWindowMs;
    private final int dosThreshold;
    private final int dosWindowMs;

    private static final int ARP_FLOOD_THRESHOLD = 30;
    private static final int ARP_FLOOD_WINDOW_MS = 10000;
    private static final int SYN_FLOOD_THRESHOLD = 50;
    private static final int SYN_FLOOD_WINDOW_MS = 5000;
    private static final int DNS_AMP_THRESHOLD = 30;
    private static final int DNS_AMP_WINDOW_MS = 5000;
    private static final int ICMP_FLOOD_THRESHOLD = 40;
    private static final int ICMP_FLOOD_WINDOW_MS = 5000;
    private static final int LARGE_PACKET_BYTES = 1400;
    private static final int TTL_LOW_THRESHOLD = 5;
    private static final int FIN_SCAN_THRESHOLD = 5;
    private static final int FIN_SCAN_WINDOW_MS = 10000;
    private static final int NULL_SCAN_THRESHOLD = 5;
    private static final int NULL_SCAN_WINDOW_MS = 10000;

    // Well-known suspicious/malicious ports
    private static final Set<Integer> SUSPICIOUS_PORTS = Set.of(
        4444,   // Metasploit default
        5555,   // ADB remote
        6666,   // IRC backdoor
        6667,   // IRC
        31337,  // Back Orifice
        1234,   // Common trojan
        12345,  // NetBus
        27374,  // SubSeven
        20000,  // DNP3
        65535   // Max port - often used in scanning
    );

    public ThreatDetector() {
        this.portScanThreshold = ConfigManager.getPortScanThreshold();
        this.portScanWindowMs = ConfigManager.getPortScanWindowSeconds() * 1000;
        this.dosThreshold = ConfigManager.getDosThreshold();
        this.dosWindowMs = ConfigManager.getDosWindowSeconds() * 1000;
    }

    public void addAlertListener(AlertListener listener) { listeners.add(listener); }

    public void analyzePacket(Packet packet) {
        long now = System.currentTimeMillis();

        // Layer 2 checks
        checkArpSpoofing(packet);
        checkArpFlood(packet, now);

        // Layer 3+ checks require IP
        IpV4Packet ipv4 = packet.get(IpV4Packet.class);
        if (ipv4 == null) return;

        String srcIp = ipv4.getHeader().getSrcAddr().getHostAddress();
        String dstIp = ipv4.getHeader().getDstAddr().getHostAddress();
        int ttl = ipv4.getHeader().getTtlAsInt();

        // Volume-based
        checkHighVolume(srcIp, now);

        // TTL anomaly
        checkTTLAnomaly(srcIp, ttl);

        // Large packet
        checkLargePacket(srcIp, dstIp, packet.length());

        // ICMP checks
        IcmpV4CommonPacket icmp = packet.get(IcmpV4CommonPacket.class);
        if (icmp != null) {
            checkICMPFlood(srcIp, now);
        }

        // TCP checks
        TcpPacket tcp = packet.get(TcpPacket.class);
        if (tcp != null) {
            int srcPort = tcp.getHeader().getSrcPort().valueAsInt();
            int dstPort = tcp.getHeader().getDstPort().valueAsInt();
            boolean syn = tcp.getHeader().getSyn();
            boolean ack = tcp.getHeader().getAck();
            boolean fin = tcp.getHeader().getFin();
            boolean rst = tcp.getHeader().getRst();
            boolean urg = tcp.getHeader().getUrg();
            boolean psh = tcp.getHeader().getPsh();

            // Port scan (SYN-only)
            if (syn && !ack) {
                checkPortScan(srcIp, dstPort, now);
                checkSYNFlood(srcIp, dstIp, now);
            }

            // FIN scan (FIN-only, no ACK)
            if (fin && !ack && !syn) {
                checkFINScan(srcIp, dstPort, now);
            }

            // NULL scan (no flags set)
            if (!syn && !ack && !fin && !rst && !urg && !psh) {
                checkNULLScan(srcIp, dstPort, now);
            }

            // Suspicious ports
            checkSuspiciousPort(srcIp, dstIp, srcPort, dstPort);
        }

        // UDP checks
        UdpPacket udp = packet.get(UdpPacket.class);
        if (udp != null) {
            int dstPort = udp.getHeader().getDstPort().valueAsInt();
            int srcPort = udp.getHeader().getSrcPort().valueAsInt();

            // DNS amplification (port 53)
            if (srcPort == 53 || dstPort == 53) {
                checkDNSAmplification(srcIp, now, packet.length());
            }

            // Suspicious ports on UDP too
            checkSuspiciousPort(srcIp, dstIp, srcPort, dstPort);
        }
    }

    // ────────── PORT SCAN (SYN sweep to many ports) ──────────
    private void checkPortScan(String srcIp, int dstPort, long now) {
        List<long[]> entries = synTracker.computeIfAbsent(srcIp, k -> Collections.synchronizedList(new ArrayList<>()));
        entries.add(new long[]{now, dstPort});
        entries.removeIf(e -> e[0] < now - portScanWindowMs);
        Set<Integer> uniquePorts = new HashSet<>();
        for (long[] e : entries) uniquePorts.add((int) e[1]);
        if (uniquePorts.size() > portScanThreshold && !portScanAlerted.contains(srcIp)) {
            portScanAlerted.add(srcIp);
            fireAlert(SEVERITY_HIGH, "PORT SCAN DETECTED from " + srcIp + " (" + uniquePorts.size() + " ports in " + (portScanWindowMs / 1000) + "s)");
            scheduleRemoval(portScanAlerted, srcIp, portScanWindowMs * 2L);
        }
    }

    // ────────── ARP SPOOFING ──────────
    private void checkArpSpoofing(Packet packet) {
        ArpPacket arp = packet.get(ArpPacket.class);
        if (arp == null) return;
        String ip = arp.getHeader().getSrcProtocolAddr().getHostAddress();
        String mac = arp.getHeader().getSrcHardwareAddr().toString();
        Set<String> macs = arpTable.computeIfAbsent(ip, k -> ConcurrentHashMap.newKeySet());
        macs.add(mac);
        if (macs.size() > 1 && !arpSpoofAlerted.contains(ip)) {
            arpSpoofAlerted.add(ip);
            fireAlert(SEVERITY_HIGH, "ARP SPOOFING DETECTED for " + ip + " (MACs: " + String.join(", ", macs) + ")");
        }
    }

    // ────────── ARP FLOOD ──────────
    private void checkArpFlood(Packet packet, long now) {
        ArpPacket arp = packet.get(ArpPacket.class);
        if (arp == null) return;
        String mac = arp.getHeader().getSrcHardwareAddr().toString();
        List<Long> ts = arpFloodTracker.computeIfAbsent(mac, k -> Collections.synchronizedList(new ArrayList<>()));
        ts.add(now);
        ts.removeIf(t -> t < now - ARP_FLOOD_WINDOW_MS);
        if (ts.size() > ARP_FLOOD_THRESHOLD && !arpFloodAlerted.contains(mac)) {
            arpFloodAlerted.add(mac);
            String ip = arp.getHeader().getSrcProtocolAddr().getHostAddress();
            fireAlert(SEVERITY_HIGH, "ARP FLOOD DETECTED from " + mac + " (IP: " + ip + ", " + ts.size() + " ARP packets in " + (ARP_FLOOD_WINDOW_MS / 1000) + "s)");
            scheduleRemoval(arpFloodAlerted, mac, ARP_FLOOD_WINDOW_MS * 2L);
        }
    }

    // ────────── DoS / HIGH VOLUME ──────────
    private void checkHighVolume(String srcIp, long now) {
        List<Long> ts = volumeTracker.computeIfAbsent(srcIp, k -> Collections.synchronizedList(new ArrayList<>()));
        ts.add(now);
        ts.removeIf(t -> t < now - dosWindowMs);
        if (ts.size() > dosThreshold && !dosAlerted.contains(srcIp)) {
            dosAlerted.add(srcIp);
            fireAlert(SEVERITY_HIGH, "POSSIBLE DoS from " + srcIp + " (" + ts.size() + " packets in " + (dosWindowMs / 1000) + "s)");
            scheduleRemoval(dosAlerted, srcIp, dosWindowMs * 2L);
        }
    }

    // ────────── SYN FLOOD (many SYNs to same target) ──────────
    private void checkSYNFlood(String srcIp, String dstIp, long now) {
        String key = srcIp + "->" + dstIp;
        List<Long> ts = synFloodTracker.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()));
        ts.add(now);
        ts.removeIf(t -> t < now - SYN_FLOOD_WINDOW_MS);
        if (ts.size() > SYN_FLOOD_THRESHOLD && !synFloodAlerted.contains(key)) {
            synFloodAlerted.add(key);
            fireAlert(SEVERITY_HIGH, "SYN FLOOD DETECTED from " + srcIp + " targeting " + dstIp + " (" + ts.size() + " SYN packets in " + (SYN_FLOOD_WINDOW_MS / 1000) + "s)");
            scheduleRemoval(synFloodAlerted, key, SYN_FLOOD_WINDOW_MS * 2L);
        }
    }

    // ────────── DNS AMPLIFICATION ──────────
    private void checkDNSAmplification(String srcIp, long now, int packetLen) {
        List<Long> ts = dnsTracker.computeIfAbsent(srcIp, k -> Collections.synchronizedList(new ArrayList<>()));
        ts.add(now);
        ts.removeIf(t -> t < now - DNS_AMP_WINDOW_MS);
        if (ts.size() > DNS_AMP_THRESHOLD && !dnsAlerted.contains(srcIp)) {
            dnsAlerted.add(srcIp);
            fireAlert(SEVERITY_HIGH, "DNS AMPLIFICATION suspected from " + srcIp + " (" + ts.size() + " DNS packets in " + (DNS_AMP_WINDOW_MS / 1000) + "s, last pkt " + packetLen + " bytes)");
            scheduleRemoval(dnsAlerted, srcIp, DNS_AMP_WINDOW_MS * 2L);
        }
    }

    // ────────── ICMP FLOOD ──────────
    private void checkICMPFlood(String srcIp, long now) {
        List<Long> ts = icmpTracker.computeIfAbsent(srcIp, k -> Collections.synchronizedList(new ArrayList<>()));
        ts.add(now);
        ts.removeIf(t -> t < now - ICMP_FLOOD_WINDOW_MS);
        if (ts.size() > ICMP_FLOOD_THRESHOLD && !icmpAlerted.contains(srcIp)) {
            icmpAlerted.add(srcIp);
            fireAlert(SEVERITY_HIGH, "ICMP FLOOD DETECTED from " + srcIp + " (" + ts.size() + " ICMP packets in " + (ICMP_FLOOD_WINDOW_MS / 1000) + "s)");
            scheduleRemoval(icmpAlerted, srcIp, ICMP_FLOOD_WINDOW_MS * 2L);
        }
    }

    // ────────── SUSPICIOUS PORT ACCESS ──────────
    private void checkSuspiciousPort(String srcIp, String dstIp, int srcPort, int dstPort) {
        if (SUSPICIOUS_PORTS.contains(dstPort)) {
            String key = srcIp + ":" + dstPort;
            if (!suspiciousPortAlerted.contains(key)) {
                suspiciousPortAlerted.add(key);
                fireAlert(SEVERITY_MEDIUM, "SUSPICIOUS PORT ACCESS from " + srcIp + " to " + dstIp + " on port " + dstPort + " (known malware/backdoor port)");
            }
        }
        if (SUSPICIOUS_PORTS.contains(srcPort)) {
            String key = srcIp + ":src:" + srcPort;
            if (!suspiciousPortAlerted.contains(key)) {
                suspiciousPortAlerted.add(key);
                fireAlert(SEVERITY_MEDIUM, "SUSPICIOUS PORT TRAFFIC from " + srcIp + " source port " + srcPort + " to " + dstIp + " (known malware/backdoor port)");
            }
        }
    }

    // ────────── LARGE PACKET DETECTION ──────────
    private void checkLargePacket(String srcIp, String dstIp, int len) {
        if (len > LARGE_PACKET_BYTES) {
            String key = srcIp + "-" + len;
            if (!largePacketAlerted.contains(key)) {
                largePacketAlerted.add(key);
                fireAlert(SEVERITY_MEDIUM, "LARGE PACKET DETECTED from " + srcIp + " to " + dstIp + " (" + len + " bytes, threshold " + LARGE_PACKET_BYTES + " bytes)");
            }
        }
    }

    // ────────── TTL ANOMALY ──────────
    private void checkTTLAnomaly(String srcIp, int ttl) {
        if (ttl <= TTL_LOW_THRESHOLD && ttl > 0) {
            String key = srcIp + "-ttl-" + ttl;
            if (!ttlAlerted.contains(key)) {
                ttlAlerted.add(key);
                fireAlert(SEVERITY_MEDIUM, "TTL ANOMALY from " + srcIp + " (TTL=" + ttl + ", threshold<=" + TTL_LOW_THRESHOLD + ") — possible traceroute or evasion");
            }
        }
    }

    // ────────── FIN SCAN ──────────
    private void checkFINScan(String srcIp, int dstPort, long now) {
        List<long[]> entries = finTracker.computeIfAbsent(srcIp, k -> Collections.synchronizedList(new ArrayList<>()));
        entries.add(new long[]{now, dstPort});
        entries.removeIf(e -> e[0] < now - FIN_SCAN_WINDOW_MS);
        Set<Integer> uniquePorts = new HashSet<>();
        for (long[] e : entries) uniquePorts.add((int) e[1]);
        if (uniquePorts.size() > FIN_SCAN_THRESHOLD && !finScanAlerted.contains(srcIp)) {
            finScanAlerted.add(srcIp);
            fireAlert(SEVERITY_HIGH, "FIN SCAN DETECTED from " + srcIp + " (" + uniquePorts.size() + " ports in " + (FIN_SCAN_WINDOW_MS / 1000) + "s)");
            scheduleRemoval(finScanAlerted, srcIp, FIN_SCAN_WINDOW_MS * 2L);
        }
    }

    // ────────── NULL SCAN ──────────
    private void checkNULLScan(String srcIp, int dstPort, long now) {
        List<long[]> entries = nullTracker.computeIfAbsent(srcIp, k -> Collections.synchronizedList(new ArrayList<>()));
        entries.add(new long[]{now, dstPort});
        entries.removeIf(e -> e[0] < now - NULL_SCAN_WINDOW_MS);
        Set<Integer> uniquePorts = new HashSet<>();
        for (long[] e : entries) uniquePorts.add((int) e[1]);
        if (uniquePorts.size() > NULL_SCAN_THRESHOLD && !nullScanAlerted.contains(srcIp)) {
            nullScanAlerted.add(srcIp);
            fireAlert(SEVERITY_HIGH, "NULL SCAN DETECTED from " + srcIp + " (" + uniquePorts.size() + " ports in " + (NULL_SCAN_WINDOW_MS / 1000) + "s)");
            scheduleRemoval(nullScanAlerted, srcIp, NULL_SCAN_WINDOW_MS * 2L);
        }
    }

    // ────────── Utility ──────────
    private void fireAlert(String severity, String message) {
        String ts = sdf.format(new Date());
        for (AlertListener l : listeners) l.onAlert(ts, severity, message);
    }

    private void scheduleRemoval(Set<String> set, String key, long delayMs) {
        new Timer(true).schedule(new TimerTask() { public void run() { set.remove(key); } }, delayMs);
    }

    public void reset() {
        synTracker.clear(); arpTable.clear(); volumeTracker.clear(); arpFloodTracker.clear();
        synFloodTracker.clear(); dnsTracker.clear(); icmpTracker.clear();
        finTracker.clear(); nullTracker.clear();
        portScanAlerted.clear(); arpSpoofAlerted.clear(); dosAlerted.clear();
        arpFloodAlerted.clear(); synFloodAlerted.clear(); dnsAlerted.clear();
        icmpAlerted.clear(); suspiciousPortAlerted.clear(); largePacketAlerted.clear();
        ttlAlerted.clear(); finScanAlerted.clear(); nullScanAlerted.clear();
    }
}
