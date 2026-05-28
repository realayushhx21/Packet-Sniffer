package com.first;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;

import java.io.*;
import java.util.List;

/**
 * Feature 6: Multi-Format Export (CSV, JSON, PCAP)
 */
public class ExportManager {

    /**
     * Export packets to CSV format.
     */
    public static void exportCSV(List<Packet> packets, File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("timestamp,src_ip,dst_ip,protocol,length,summary");
            String ts = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            for (Packet pkt : packets) {
                String[] info = PacketUtils.extractPacketInfo(pkt);
                pw.println(escapeCsv(ts) + "," +
                        escapeCsv(info[0]) + "," +
                        escapeCsv(info[1]) + "," +
                        escapeCsv(info[2]) + "," +
                        pkt.length() + "," +
                        escapeCsv(info[3]));
            }
        }
    }

    /**
     * Export packets to JSON format.
     */
    public static void exportJSON(List<Packet> packets, File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("[");
            String ts = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            for (int idx = 0; idx < packets.size(); idx++) {
                Packet pkt = packets.get(idx);
                String[] info = PacketUtils.extractPacketInfo(pkt);
                pw.println("  {");
                pw.println("    \"timestamp\": " + escapeJson(ts) + ",");
                pw.println("    \"src_ip\": " + escapeJson(info[0]) + ",");
                pw.println("    \"dst_ip\": " + escapeJson(info[1]) + ",");
                pw.println("    \"protocol\": " + escapeJson(info[2]) + ",");
                pw.println("    \"length\": " + pkt.length() + ",");
                pw.println("    \"summary\": " + escapeJson(info[3]));
                pw.println("  }" + (idx < packets.size() - 1 ? "," : ""));
            }
            pw.println("]");
        }
    }

    /**
     * Export packets to PCAP format.
     */
    public static void exportPCAP(List<Packet> packets, PcapHandle handle, File file) throws PcapNativeException, NotOpenException {
        PcapDumper dumper = handle.dumpOpen(file.getAbsolutePath());
        java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        for (Packet pkt : packets) {
            dumper.dump(pkt, timestamp);
        }
        dumper.close();
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String escapeJson(String value) {
        if (value == null) return "null";
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }
}
