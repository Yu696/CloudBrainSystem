-- =============================================================
-- 迁移脚本：drug_stock 唯一约束变更
-- 日期：2026-06-29
-- 说明：支持一个药品在多个仓库有独立库存记录
-- 原：UNIQUE KEY uk_drug_id (drug_id)
-- 新：UNIQUE KEY uk_drug_warehouse (drug_id, warehouse_id)
-- =============================================================

USE cloudbrain;

-- 1. 删除旧的唯一索引
ALTER TABLE drug_stock DROP INDEX uk_drug_id;

-- 2. 创建新的联合唯一索引
ALTER TABLE drug_stock ADD UNIQUE KEY uk_drug_warehouse (drug_id, warehouse_id);

-- 3. 检查是否有冲突数据（应该没有，当前每个药品只在 WH_001 有一条记录）
SELECT drug_id, warehouse_id, COUNT(*) as cnt
FROM drug_stock
GROUP BY drug_id, warehouse_id
HAVING cnt > 1;
