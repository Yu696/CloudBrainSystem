-- =============================================================
-- 阶段三 种子数据
-- 模块：M05 影像管理 + M07 药库管理
-- 执行顺序：在 06-image.sql 和 07-pharmacy.sql 之后执行
-- =============================================================

SET NAMES utf8mb4;
USE cloudbrain;

-- ==================== M05 影像管理 种子数据 ====================

-- 存储配置种子（本地文件系统 + PACS 预留）
INSERT INTO storage_config (config_id, storage_type, endpoint, bucket, max_capacity, used_capacity, priority, status) VALUES
('SC_001', 0, '/data/cloudbrain/images', NULL, 107374182400, 0, 0, 1),
('SC_002', 1, NULL, 'cloudbrain-pacs', 53687091200, 0, 1, 0);

-- ==================== M07 药库管理 种子数据 ====================

-- 仓库种子
INSERT INTO warehouse (warehouse_id, name, location, admin_id, type, status) VALUES
('WH_001', '中心药库', '门诊楼 B1 层', NULL, 0, 1),
('WH_002', '门诊药房', '门诊楼 1 层大厅', NULL, 1, 1);

-- 药品种子
INSERT INTO drug (drug_id, drug_code, drug_name, generic_name, ingredients, spec, dosage_form, manufacturer, unit, unit_price, purchase_price, usage_method, cautious_crowd, side_effects, drug_category, prescription_type, status) VALUES
('DRUG_001', 'YP20240001', '阿莫西林胶囊', '阿莫西林', '阿莫西林', '0.5g×24粒', '胶囊剂', '华北制药', '盒', 18.50, 12.00, '口服，一次0.5g，一日3次', '青霉素过敏者禁用', '恶心、腹泻、皮疹', '抗生素', 1, 1),
('DRUG_002', 'YP20240002', '布洛芬缓释胶囊', '布洛芬', '布洛芬', '0.3g×20粒', '胶囊剂', '中美史克', '盒', 25.00, 16.50, '口服，一次0.3g，一日2次', '活动性消化性溃疡者禁用', '胃肠道不适、头痛、眩晕', '解热镇痛药', 1, 1),
('DRUG_003', 'YP20240003', '硝苯地平控释片', '硝苯地平', '硝苯地平', '30mg×7片', '片剂', '拜耳医药', '盒', 35.00, 24.00, '口服，一次30mg，一日1次', '心源性休克患者禁用', '头痛、面部潮红、下肢水肿', '心血管药物', 1, 1),
('DRUG_004', 'YP20240004', '维生素C片', '维生素C', '维生素C', '0.1g×100片', '片剂', '东北制药', '瓶', 8.00, 4.50, '口服，一次0.1g，一日2-3次', '高草酸尿症患者慎用', '大剂量可致腹泻', '维生素类', 0, 1);

-- 药品库存种子
INSERT INTO drug_stock (drug_id, warehouse_id, current_stock, min_stock, max_stock, batch_no, production_date, expiry_date, status) VALUES
('DRUG_001', 'WH_001', 150, 10, 1000, '20240501', '2024-05-01', '2026-05-01', 1),
('DRUG_002', 'WH_001', 80, 10, 500, '20240315', '2024-03-15', '2026-03-15', 1),
('DRUG_003', 'WH_001', 5, 10, 300, '20240120', '2024-01-20', '2026-01-20', 1),   -- 低库存示例（current=5 < min=10）
('DRUG_004', 'WH_001', 200, 20, 800, '20240601', '2024-06-01', '2026-12-01', 1);
