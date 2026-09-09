package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.SyncRecord;
import java.util.ArrayList;
import java.util.List;

import java.sql.*;

public class SyncQueueDAO {

    public List<SyncRecord> getSyncHistory() {
        return getSyncHistory(15);
    }

    public List<SyncRecord> getSyncHistory(int limit) {
        List<SyncRecord> history = new ArrayList<>();
        String sql = """
            SELECT
            table_name,
            operation,
            status,
            created_at
            FROM sync_queue
            ORDER BY id DESC
            LIMIT ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    SyncRecord record = new SyncRecord(
                            rs.getString("table_name"),
                            rs.getString("operation"),
                            rs.getString("status"),
                            rs.getString("created_at")
                    );
                    history.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }

    public int getPendingCount() {
        String sql = "SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getCompletedCount() {
        String sql = "SELECT COUNT(*) FROM sync_queue WHERE status = 'COMPLETED' OR status = 'SYNCED'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getFailedCount() {
        String sql = "SELECT COUNT(*) FROM sync_queue WHERE status = 'FAILED' OR (attempts > 0 AND status NOT IN ('COMPLETED', 'SYNCED'))";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getLastSyncTime() {
        String sql = "SELECT COALESCE(last_attempt_at, created_at) FROM sync_queue WHERE status IN ('COMPLETED', 'SYNCED') ORDER BY COALESCE(last_attempt_at, created_at) DESC, id DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String time = rs.getString(1);
                if (time != null && !time.trim().isEmpty()) {
                    return time;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.util.Map<String, Integer> getPendingCountsByModule() {
        java.util.Map<String, Integer> moduleCounts = new java.util.LinkedHashMap<>();
        moduleCounts.put("products", 0);
        moduleCounts.put("customers", 0);
        moduleCounts.put("suppliers", 0);
        moduleCounts.put("purchases", 0);
        moduleCounts.put("sales", 0);
        moduleCounts.put("expenses", 0);
        moduleCounts.put("transactions", 0);

        String sql = "SELECT table_name, COUNT(*) FROM sync_queue WHERE status = 'PENDING' GROUP BY table_name";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String tbl = rs.getString(1);
                int count = rs.getInt(2);
                if (tbl != null) {
                    moduleCounts.put(tbl.toLowerCase(), count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return moduleCounts;
    }
    
    
    public void addToQueue(
            String tableName,
            int recordId,
            String operation
    ){
        try (Connection connection = DatabaseManager.getConnection()) {
            addToQueue(connection, tableName, recordId, operation);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void addToQueue(
            Connection connection,
            String tableName,
            int recordId,
            String operation
    ) throws SQLException {
        String sql =
        """
        INSERT INTO sync_queue
        (table_name, record_id, operation, status, attempts)
        VALUES (?, ?, ?, 'PENDING', 0)
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setInt(2, recordId);
            statement.setString(3, operation);
            statement.executeUpdate();
        }
    }
}