-- =============================================================
-- 迁移脚本：drug_stock 唯一约束增加 batch_no
-- 日期：2026-06-29
-- 说明：支持按批次管理库存，同一药品同一仓库不同批次独立记录
-- 原：UNIQUE KEY uk_drug_warehouse (drug_id, warehouse_id)
-- 新：UNIQUE KEY uk_drug_warehouse_batch (drug_id, warehouse_id, batch_no)
-- =============================================================

USE cloudbrain;

-- 1. 将已有 NULL 批号改为占位值
UPDATE drug_stock SET batch_no = CONCAT('LEGACY-', drug_id) WHERE batch_no IS NULL OR batch_no = '';

-- 2. 删除旧唯一索引（若存在）
SET @index_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'cloudbrain' AND TABLE_NAME = 'drug_stock' AND INDEX_NAME = 'uk_drug_warehouse');
SET @drop_sql = IF(@index_exists > 0, 'ALTER TABLE drug_stock DROP INDEX uk_drug_warehouse', 'SELECT 1');
PREPARE stmt FROM @drop_sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. 创建新的联合唯一索引（含批号，若不存在）
SET @index_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'cloudbrain' AND TABLE_NAME = 'drug_stock' AND INDEX_NAME = 'uk_drug_warehouse_batch');
SET @add_sql = IF(@index_exists = 0, 'ALTER TABLE drug_stock ADD UNIQUE KEY uk_drug_warehouse_batch (drug_id, warehouse_id, batch_no)', 'SELECT 1');
PREPARE stmt FROM @add_sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4. 过期批次演示数据（库存仍在，供系统自动检测和销毁）
INSERT IGNORE INTO drug_stock (drug_id, warehouse_id, current_stock, min_stock, max_stock, batch_no, production_date, expiry_date, status) VALUES
('DRUG_001', 'WH_001', 30, 10, 1000, 'EXP202501', '2025-01-10', '2025-06-01', 1),
('DRUG_002', 'WH_001', 20, 10, 500, 'EXP202503', '2025-03-01', '2025-08-15', 1);

-- =============================================================
-- 库存预警测试数据（覆盖批号维度下的预警场景）
-- 前提：12-phase3-data.sql 已执行
-- =============================================================

-- 场景1: 同仓库多批次 — 只有部分过期
--   背景：DRUG_001 在 WH_001 已有批次 20240501（150 盒，2027-05 到期）
--   新增：正常批次 20260601（100 盒）+ 已存在的过期批次 EXP202501（30 盒）
--   验证：点击销毁 DRUG_001 过期批次时，仅 EXP202501 清零，20240501 和 20260601 不受影响
INSERT IGNORE INTO drug_stock (drug_id, warehouse_id, current_stock, min_stock, max_stock, batch_no, production_date, expiry_date, status) VALUES
('DRUG_001', 'WH_001', 100, 10, 1000, '20260601', '2026-06-01', '2028-06-01', 1);

-- 场景2: 跨仓库过期 — 销毁只影响指定仓库
--   背景：DRUG_002 在 WH_001 已有批次 20240315（80 盒，正常）
--   新增：WH_002 中一个过期批次 EXP2409（15 盒，2024-09 到期）
--   验证：在 WH_002 点击销毁，只清零 WH_002 的过期批次，不碰 WH_001 的库存
INSERT IGNORE INTO drug_stock (drug_id, warehouse_id, current_stock, min_stock, max_stock, batch_no, production_date, expiry_date, status) VALUES
('DRUG_002', 'WH_002', 15, 5, 500, 'EXP2409', '2023-12-01', '2024-09-01', 1);

-- 场景3: 同仓库同药品多过期批次 — 过期预警去重
--   背景：DRUG_003 在 WH_001 已有批次 20240120（5 盒，2026-01 到期，已过期）
--   新增：过期批次 EXP2401（10 盒，2024-01 到期）
--   验证：过期预警按 (drug_id, warehouse_id) 去重，只产生一条过期预警
INSERT IGNORE INTO drug_stock (drug_id, warehouse_id, current_stock, min_stock, max_stock, batch_no, production_date, expiry_date, status) VALUES
('DRUG_003', 'WH_001', 10, 10, 300, 'EXP2401', '2023-06-01', '2024-01-01', 1);

-- 场景4: 库存积压 — 多批次合计超上限
--   背景：DRUG_004 在 WH_001 已有批次 20240601（200 盒，max=800）
--   新增：大批次 20250201（700 盒）→ 总量 900 > 800，触发积压预警
--   验证：adjustStock / transferStock 后检测到总量 > max_stock，生成 type=2 预警
INSERT IGNORE INTO drug_stock (drug_id, warehouse_id, current_stock, min_stock, max_stock, batch_no, production_date, expiry_date, status) VALUES
('DRUG_004', 'WH_001', 700, 20, 800, '20250201', '2025-02-01', '2028-02-01', 1);

-- 场景5: 过期且库存为 0 — 不应出现在过期预警列表
--   说明：该批次已过期且库存已清零，当做"已处理"的历史数据
--   验证：listAlerts(type=1) 查询带有 .gt(CurrentStock, 0) 条件，此条被正确排除
INSERT IGNORE INTO drug_stock (drug_id, warehouse_id, current_stock, min_stock, max_stock, batch_no, production_date, expiry_date, status) VALUES
('DRUG_004', 'WH_001', 0, 10, 1000, 'EXP2312', '2022-06-01', '2023-12-01', 1);
