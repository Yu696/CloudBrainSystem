-- ============================================================
-- 18-cleanup-orphan-stock.sql
-- 清理 drug_stock 中的孤儿记录（药品已不存在的库存）
-- 以及修复 create_time 为空的历史数据
-- ============================================================

-- 1. 清理孤儿库存记录：drug_id 在 drug 表中不存在的 drug_stock
DELETE FROM drug_stock
WHERE drug_id NOT IN (SELECT drug_id FROM drug);

-- 2. 修复 create_time 为空的库存记录
UPDATE drug_stock SET create_time = NOW()
WHERE create_time IS NULL;

-- 3. 同理清理 stock_alert 中的孤儿预警记录
DELETE FROM stock_alert
WHERE drug_id NOT IN (SELECT drug_id FROM drug);
