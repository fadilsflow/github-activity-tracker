package com.github.repo.tracker.db;

import com.github.repo.tracker.model.Repo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepoDatabase {

    private static final String DB_URL = "jdbc:mysql://localhost:8889/github_tracker?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    public RepoDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver tidak ditemukan");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS repo (id BIGINT PRIMARY KEY, username VARCHAR(255), name VARCHAR(255), description TEXT, language VARCHAR(100), stars INT, forks INT, private TINYINT(1), updated_at DATETIME, html_url VARCHAR(500), hash VARCHAR(64))");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveRepos(String username, List<Repo> repos, String hash) {
        String sql = "REPLACE INTO repo(id, username, name, description, language, stars, forks, private, updated_at, html_url, hash) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Repo repo : repos) {
                    ps.setLong(1, repo.getId());
                    ps.setString(2, username);
                    ps.setString(3, repo.getName());
                    ps.setString(4, repo.getDescription());
                    ps.setString(5, repo.getLanguage());
                    ps.setInt(6, repo.getStargazersCount());
                    ps.setInt(7, repo.getForksCount());
                    ps.setBoolean(8, repo.isPrivate());
                    ps.setTimestamp(9, Timestamp.from(repo.getUpdatedAt()));
                    ps.setString(10, repo.getHtmlUrl());
                    ps.setString(11, hash);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Repo> loadRepos(String username) {
        List<Repo> list = new ArrayList<>();
        String sql = "SELECT * FROM repo WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Repo repo = new Repo();
                    repo.setId(rs.getLong("id"));
                    repo.setName(rs.getString("name"));
                    repo.setDescription(rs.getString("description"));
                    repo.setLanguage(rs.getString("language"));
                    repo.setStargazersCount(rs.getInt("stars"));
                    repo.setForksCount(rs.getInt("forks"));
                    repo.setPrivate(rs.getInt("private") == 1);
                    repo.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                    repo.setHtmlUrl(rs.getString("html_url"));
                    list.add(repo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
} 