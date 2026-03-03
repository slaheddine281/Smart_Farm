-- =====================================================
-- Smart Farm Database Schema
-- Database: smart_farm
-- Run this script in MySQL / phpMyAdmin to create the
-- required table structure.
-- =====================================================

CREATE DATABASE IF NOT EXISTS smart_farm
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_farm;

CREATE TABLE IF NOT EXISTS users (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    username            VARCHAR(100)  NOT NULL,
    email               VARCHAR(150)  NOT NULL UNIQUE,
    password_hash       VARCHAR(255)  NOT NULL,
    role                VARCHAR(50)   NOT NULL DEFAULT 'USER',
    photo_professionelle VARCHAR(500) DEFAULT NULL,
    verification_code   VARCHAR(10)   DEFAULT NULL,
    created_at          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- Optional: insert a default admin account (password = "admin123")
-- INSERT INTO users (username, email, password_hash, role)
-- VALUES ('Admin', 'admin@smartfarm.com', 'admin123', 'ADMIN');
