-- =============================================================
-- 迁移脚本：stock_alert 增加 batch_no
-- 日期：2026-06-30
-- 说明：支持按批次追踪预警，区分同一药品同一仓库不同批次的预警
-- =============================================================

USE cloudbrain;

ALTER TABLE stock_alert ADD COLUMN `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批号' AFTER `warehouse_id`;
