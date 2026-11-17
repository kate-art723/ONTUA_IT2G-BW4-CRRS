package com.scc.dao;

import com.scc.Model.ActivityLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public void logActivity(int userId, String action) {
        String sql = "INSERT INTO activity_logs (user_id, action) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, action);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }

    public List<ActivityLog> getAllLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = """
            SELECT a.id, a.user_id, u.username, a.action, a.timestamp
            FROM activity_logs a
            JOIN users u ON a.user_id = u.id
            ORDER BY a.timestamp DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(mapRowToActivityLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving logs: " + e.getMessage());
        }
        return logs;
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = """
            SELECT a.id, a.user_id, u.username, a.action, a.timestamp
            FROM activity_logs a
            JOIN users u ON a.user_id = u.id
            WHERE a.user_id = ?
            ORDER BY a.timestamp DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToActivityLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user logs: " + e.getMessage());
        }
        return logs;
    }

    public boolean clearAllLogs() {
        String sql = "DELETE FROM activity_logs";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Error clearing logs: " + e.getMessage());
            return false;
        }
    }

    private ActivityLog mapRowToActivityLog(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("action"),
                rs.getString("timestamp")
        );
    }
}