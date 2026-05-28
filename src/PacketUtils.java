package com.first;

import org.pcap4j.packet.*;

/**
 * Utility class for extracting structured info from pcap4j Packet objects.
 * Used by DatabaseManager, ExportManager, and chart/protocol detection.
 * All methods are null-safe and will never return null array elements.
 */
public class PacketUtils {

    /**
     * Extract key info from a packet.
     * @return String array: [srcIp, dstIp, protocol, summary] — never null elements
     */
    public static String[] extractPacketInfo(Packet packet) {
        if (packet == null) {
            return new String[]{"N/A", "N/A", "Unknown", "Empty packet"};
        }

        String srcIp = "N/A";
        String dstIp = "N/A";
        String protocol = "Other";
        String summary = "";

        try {
            // Check for IPv4
            IpV4Packet ipv4 = packet.get(IpV4Packet.class);
            if (ipv4 != null && ipv4.getHeader() != null) {
                srcIp = safeStr(ipv4.getHeader().getSrcAddr() != null ?
                        ipv4.getHeader().getSrcAddr().getHostAddress() : "N/A");
                dstIp = safeStr(ipv4.getHeader().getDstAddr() != null ?
                        ipv4.getHeader().getDstAddr().getHostAddress() : "N/A");
                protocol = "IPv4";

                // Check for TCP
                TcpPacket tcp = packet.get(TcpPacket.class);
                if (tcp != null && tcp.getHeader() != null) {
                    protocol = "TCP";
                    int srcPort = tcp.getHeader().getSrcPort() != null ? tcp.getHeader().getSrcPort().valueAsInt() : 0;
                    int dstPort = tcp.getHeader().getDstPort() != null ? tcp.getHeader().getDstPort().valueAsInt() : 0;
                    summary = srcIp + ":" + srcPort + " -> " + dstIp + ":" + dstPort;
                    try {
                        if (tcp.getHeader().getSyn()) summary += " [SYN]";
                        if (tcp.getHeader().getAck()) summary += " [ACK]";
                        if (tcp.getHeader().getFin()) summary += " [FIN]";
                        if (tcp.getHeader().getRst()) summary += " [RST]";
                    } catch (Exception ignored) {}
                    return new String[]{srcIp, dstIp, protocol, summary};
                }

                // Check for UDP
                UdpPacket udp = packet.get(UdpPacket.class);
                if (udp != null && udp.getHeader() != null) {
                    protocol = "UDP";
                    int srcPort = udp.getHeader().getSrcPort() != null ? udp.getHeader().getSrcPort().valueAsInt() : 0;
                    int dstPort = udp.getHeader().getDstPort() != null ? udp.getHeader().getDstPort().valueAsInt() : 0;
                    summary = srcIp + ":" + srcPort + " -> " + dstIp + ":" + dstPort;
                    return new String[]{srcIp, dstIp, protocol, summary};
                }

                // Check for ICMP
                IcmpV4CommonPacket icmp = packet.get(IcmpV4CommonPacket.class);
                if (icmp != null && icmp.getHeader() != null) {
                    protocol = "ICMP";
                    summary = srcIp + " -> " + dstIp + " ICMP " + safeStr(String.valueOf(icmp.getHeader().getType()));
                    return new String[]{srcIp, dstIp, protocol, summary};
                }

                summary = srcIp + " -> " + dstIp;
                return new String[]{srcIp, dstIp, protocol, summary};
            }

            // Check for IPv6
            IpV6Packet ipv6 = packet.get(IpV6Packet.class);
            if (ipv6 != null && ipv6.getHeader() != null) {
                srcIp = safeStr(ipv6.getHeader().getSrcAddr() != null ?
                        ipv6.getHeader().getSrcAddr().getHostAddress() : "N/A");
                dstIp = safeStr(ipv6.getHeader().getDstAddr() != null ?
                        ipv6.getHeader().getDstAddr().getHostAddress() : "N/A");
                protocol = "IPv6";
                summary = srcIp + " -> " + dstIp;

                if (packet.get(TcpPacket.class) != null) protocol = "TCP";
                else if (packet.get(UdpPacket.class) != null) protocol = "UDP";

                return new String[]{srcIp, dstIp, protocol, summary};
            }

            // Check for ARP
            ArpPacket arp = packet.get(ArpPacket.class);
            if (arp != null && arp.getHeader() != null) {
                protocol = "ARP";
                srcIp = safeStr(arp.getHeader().getSrcProtocolAddr() != null ?
                        arp.getHeader().getSrcProtocolAddr().getHostAddress() : "N/A");
                dstIp = safeStr(arp.getHeader().getDstProtocolAddr() != null ?
                        arp.getHeader().getDstProtocolAddr().getHostAddress() : "N/A");
                summary = "ARP " + srcIp + " -> " + dstIp;
                return new String[]{srcIp, dstIp, protocol, summary};
            }
        } catch (Exception e) {
            // Catch any unexpected exceptions from packet parsing
            protocol = "Unknown";
            summary = "Parse error: " + e.getMessage();
        }

        // Fallback
        if (summary.isEmpty()) {
            summary = packet.getClass().getSimpleName() + " (" + packet.length() + " bytes)";
        }
        return new String[]{safeStr(srcIp), safeStr(dstIp), safeStr(protocol), safeStr(summary)};
    }

    /**
     * Detect the protocol name for a packet (used by charts).
     */
    public static String detectProtocol(Packet packet) {
        if (packet == null) return "Other";
        try {
            if (packet.get(TcpPacket.class) != null) return "TCP";
            if (packet.get(UdpPacket.class) != null) return "UDP";
            if (packet.get(IcmpV4CommonPacket.class) != null) return "ICMP";
            if (packet.get(ArpPacket.class) != null) return "ARP";
        } catch (Exception ignored) {}
        return "Other";
    }

    /** Ensure a string is never null */
    private static String safeStr(String s) {
        return s != null ? s : "N/A";
    }
}
