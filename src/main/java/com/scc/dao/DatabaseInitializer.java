package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        // TABLE 1: Citizens
        String createCitizensTable = """
            CREATE TABLE IF NOT EXISTS citizens (
                citizen_id INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name TEXT NOT NULL,
                middle_name TEXT,
                last_name TEXT NOT NULL,
                birth_date TEXT NOT NULL,
                gender TEXT NOT NULL,
                civil_status TEXT NOT NULL,
                address TEXT NOT NULL,
                contact_number TEXT,
                status TEXT NOT NULL DEFAULT 'ACTIVE'
            );
        """;

        // TABLE 3: Users (Created before Requests & Activity Logs due to Foreign Keys)
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'ACTIVE',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """;

        // TABLE 2: Requests
        String createRequestsTable = """
            CREATE TABLE IF NOT EXISTS requests (
                request_id INTEGER PRIMARY KEY AUTOINCREMENT,
                citizen_id INTEGER NOT NULL,
                request_type TEXT NOT NULL,
                request_date DATE DEFAULT (DATE('now', 'localtime')),
                purpose TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                processed_by INTEGER,
                release_date DATE,
                FOREIGN KEY (citizen_id) REFERENCES citizens(citizen_id),
                FOREIGN KEY (processed_by) REFERENCES users(user_id)
            );
        """;

        // TABLE 4: Activity Logs
        String createLogsTable = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                action TEXT NOT NULL,
                description TEXT,
                log_date DATE DEFAULT (DATE('now', 'localtime')),
                log_time TIME DEFAULT (TIME('now', 'localtime')),
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            );
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            // Enable Foreign Key enforcement in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            stmt.execute(createCitizensTable);
            stmt.execute(createUsersTable);
            stmt.execute(createRequestsTable);
            stmt.execute(createLogsTable);

            seedDefaultAdmin(conn);
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertAdmin = """
                    INSERT INTO users (username, password_hash, full_name, role, status)
                    VALUES (?, ?, ?, ?, ?)
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(insertAdmin)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, SecurityUtil.hashPassword("admin123"));
                    pstmt.setString(3, "System Administrator");
                    pstmt.setString(4, "ADMIN");
                    pstmt.setString(5, "ACTIVE");
                    pstmt.executeUpdate();
                }
            }
        }
    }
}