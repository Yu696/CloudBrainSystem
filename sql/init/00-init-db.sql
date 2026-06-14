-- =============================================================
-- 云脑诊疗平台 — 数据库初始化
-- 阶段一：单体架构，所有表在同一数据库 cloudbrain
-- =============================================================

CREATE DATABASE IF NOT EXISTS cloudbrain
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cloudbrain;
