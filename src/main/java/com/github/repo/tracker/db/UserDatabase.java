package com.github.repo.tracker.db;

import com.github.repo.tracker.util.HashUtil;

import java.sql.*;

public class UserDatabase {
    private static final String DB_URL = "jdbc:mysql://localhost:8889/github_tracker?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    public UserDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver tidak ditemukan");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS users (username VARCHAR(255) PRIMARY KEY, password VARCHAR(64) NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean register(String username, String password) {
        String hashedPassword = HashUtil.sha256(password);
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Username sudah ada atau error lainnya
            return false;
        }
    }

    public boolean authenticate(String username, String password) {
        String hashedPassword = HashUtil.sha256(password);
        String sql = "SELECT password FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    return hashedPassword.equals(storedHash);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 