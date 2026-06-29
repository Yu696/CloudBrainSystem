-- =============================================================
-- 迁移脚本：stock_alert 增加 warehouse_id
-- 日期：2026-06-29
-- 说明：支持按仓库统计低库存/库存积压预警
-- =============================================================

USE cloudbrain;

ALTER TABLE stock_alert
    ADD COLUMN `warehouse_id` VARCHAR(32) DEFAULT NULL COMMENT '仓库 ID' AFTER `drug_id`,
    ADD KEY `idx_warehouse_id` (`warehouse_id`);
