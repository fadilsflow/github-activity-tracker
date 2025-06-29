-- Buat database
CREATE DATABASE IF NOT EXISTS github_tracker
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE github_tracker;

-- Buat tabel repositori
CREATE TABLE IF NOT EXISTS repo (
    id          BIGINT PRIMARY KEY,
    username    VARCHAR(255),
    name        VARCHAR(255),
    description TEXT,
    language    VARCHAR(100),
    stars       INT,
    forks       INT,
    private     TINYINT(1),
    updated_at  DATETIME,
    html_url    VARCHAR(500),
    hash        VARCHAR(64)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Buat tabel pengguna
CREATE TABLE IF NOT EXISTS users (
    username    VARCHAR(255) PRIMARY KEY,
    password    VARCHAR(64) NOT NULL,  -- SHA-256 hash
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;