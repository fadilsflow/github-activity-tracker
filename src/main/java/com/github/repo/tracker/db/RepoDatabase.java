package com.github.repo.tracker.db;

import com.github.repo.tracker.model.Repo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepoDatabase {

    private static final String DB_URL = "jdbc:sqlite:repos.db";

    public RepoDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS repo (id INTEGER PRIMARY KEY, username TEXT, name TEXT, description TEXT, language TEXT, stars INTEGER, forks INTEGER, private INTEGER, updated_at TEXT, html_url TEXT, hash TEXT)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveRepos(String username, List<Repo> repos, String hash) {
        String sql = "INSERT OR REPLACE INTO repo(id, username, name, description, language, stars, forks, private, updated_at, html_url, hash) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
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
                    ps.setString(9, repo.getUpdatedAt().toString());
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
        try (Connection conn = DriverManager.getConnection(DB_URL);
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
                    repo.setUpdatedAt(java.time.Instant.parse(rs.getString("updated_at")));
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