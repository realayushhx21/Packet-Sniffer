package com.first;

import org.pcap4j.packet.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Feature 5: SQLite Persistence
 * Manages packet storage and search in an SQLite database.
 * Thread-safe: savePacket can be called from the capture thread.
 */
public class DatabaseManager {

    private Connection connection;

    public DatabaseManager() {
        try {
            String dbPath = ConfigManager.getDbPath();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            // Enable WAL mode for better concurrent read/write performance
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
            }
            connection.setAutoCommit(true);
            createTable();
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS packets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "timestamp TEXT NOT NULL, " +
                "session_id TEXT NOT NULL, " +
                "src_ip TEXT, " +
                "dst_ip TEXT, " +
                "protocol TEXT, " +
                "length INTEGER, " +
                "summary TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_session ON packets(session_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_src_ip ON packets(src_ip)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_dst_ip ON packets(dst_ip)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_protocol ON packets(protocol)");
        }
    }

    /**
     * Save a captured packet to the database. Thread-safe.
     */
    public synchronized void savePacket(Packet packet, String sessionId) {
        if (connection == null) return;
        try (PreparedStatement insertStmt = connection.prepareStatement(
                "INSERT INTO packets (timestamp, session_id, src_ip, dst_ip, protocol, length, summary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            String[] info = PacketUtils.extractPacketInfo(packet);
            insertStmt.setString(1, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date()));
            insertStmt.setString(2, sessionId);
            insertStmt.setString(3, info[0]); // src_ip
            insertStmt.setString(4, info[1]); // dst_ip
            insertStmt.setString(5, info[2]); // protocol
            insertStmt.setInt(6, packet.length());
            insertStmt.setString(7, info[3]); // summary
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save packet: " + e.getMessage());
        }
    }

    /**
     * Search packets with filters. All parameters are optional (pass null/empty to skip).
     */
    public List<String[]> searchPackets(String ipFilter, String protocolFilter, String dateFrom, String dateTo) {
        List<String[]> results = new ArrayList<>();
        if (connection == null) return results;

        StringBuilder sql = new StringBuilder("SELECT timestamp, src_ip, dst_ip, protocol, length, summary FROM packets WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (ipFilter != null && !ipFilter.trim().isEmpty()) {
            sql.append(" AND (src_ip LIKE ? OR dst_ip LIKE ?)");
            params.add("%" + ipFilter.trim() + "%");
            params.add("%" + ipFilter.trim() + "%");
        }
        if (protocolFilter != null && !protocolFilter.trim().isEmpty() && !protocolFilter.equals("All")) {
            sql.append(" AND protocol = ?");
            params.add(protocolFilter.trim());
        }
        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND timestamp >= ?");
            params.add(dateFrom.trim());
        }
        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND timestamp <= ?");
            params.add(dateTo.trim());
        }
        sql.append(" ORDER BY timestamp DESC LIMIT 1000");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int idx = 0; idx < params.size(); idx++) {
                ps.setString(idx + 1, params.get(idx));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(new String[]{
                        rs.getString("timestamp"),
                        rs.getString("src_ip"),
                        rs.getString("dst_ip"),
                        rs.getString("protocol"),
                        String.valueOf(rs.getInt("length")),
                        rs.getString("summary")
                });
            }
        } catch (SQLException e) {
            System.err.println("Search failed: " + e.getMessage());
        }
        return results;
    }

    /**
     * Get all unique session IDs for "Load Previous Session".
     */
    public List<String[]> getSessions() {
        List<String[]> sessions = new ArrayList<>();
        if (connection == null) return sessions;
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT session_id, MIN(timestamp) as start_time, COUNT(*) as pkt_count " +
                    "FROM packets GROUP BY session_id ORDER BY start_time DESC LIMIT 50");
            while (rs.next()) {
                sessions.add(new String[]{
                        rs.getString("session_id"),
                        rs.getString("start_time"),
                        String.valueOf(rs.getInt("pkt_count"))
                });
            }
        } catch (SQLException e) {
            System.err.println("Failed to get sessions: " + e.getMessage());
        }
        return sessions;
    }

    /**
     * Load packets from a previous session.
     */
    public List<String[]> loadSession(String sessionId) {
        List<String[]> results = new ArrayList<>();
        if (connection == null) return results;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT timestamp, src_ip, dst_ip, protocol, length, summary FROM packets WHERE session_id = ? ORDER BY timestamp")) {
            ps.setString(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(new String[]{
                        rs.getString("timestamp"),
                        rs.getString("src_ip"),
                        rs.getString("dst_ip"),
                        rs.getString("protocol"),
                        String.valueOf(rs.getInt("length")),
                        rs.getString("summary")
                });
            }
        } catch (SQLException e) {
            System.err.println("Failed to load session: " + e.getMessage());
        }
        return results;
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.err.println("Failed to close database: " + e.getMessage());
        }
    }
}
