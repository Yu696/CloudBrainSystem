-- =============================================================
-- 阶段三 预警演示数据
-- 模块：M07 药库管理 — 库存预警
-- 执行顺序：在 12-phase3-data.sql 之后执行
--
-- 三种预警场景：
--   type=0 低库存  → DRUG_003（current=5 < min=10）
--   type=1 过期预警 → DRUG_003（expiry=2026-01-20，已过期）
--   type=2 库存积压 → DRUG_004（下调 max_stock 至 50，当前 200 > 50）
-- =============================================================

SET NAMES utf8mb4;
USE cloudbrain;

-- 1. 调整库存数据，制造库存积压场景
-- DRUG_004（维生素C片）当前库存 200，下调 max_stock 至 50 触发积压预警
UPDATE drug_stock SET max_stock = 50 WHERE drug_id = 'DRUG_004';

-- 2. 插入预警记录
INSERT INTO stock_alert (drug_id, alert_type, current_stock, threshold, alert_message, is_handled, create_time) VALUES
-- 低库存预警：DRUG_003 current=5 < min=10
('DRUG_003', 0, 5, 10, '硝苯地平控释片库存低于最低预警线（当前: 5，预警线: 10）', 0, NOW()),

-- 过期预警：DRUG_003 有效期至 2026-01-20，已过期
('DRUG_003', 1, 5, 10, '硝苯地平控释片已过期（有效期至: 2026-01-20）', 0, NOW()),

-- 库存积压预警：DRUG_004 current=200 > max=50
('DRUG_004', 2, 200, 50, '维生素C片库存积压（当前: 200，上限: 50）', 0, NOW());
