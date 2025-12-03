-- V3__Additional_Seed_Data.sql
-- Device Management System - Additional seed data to match frontend requirements

-- Add more users to match frontend data
INSERT INTO t_user (staff_id, username, password, role, status, email, phone) VALUES
('E005', '孙七', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'sunqi@company.com', '13800000006'),
('E006', '周八', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'zhouba@company.com', '13800000007'),
('E007', '吴九', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'wujiu@company.com', '13800000008'),
('E008', '郑十', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'zhengshi@company.com', '13800000009'),
('E009', '冯十一', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'fengshiyi@company.com', '13800000010'),
('E010', '陈十二', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'chenshier@company.com', '13800000011'),
('E011', '卫十三', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'weishisan@company.com', '13800000012');

-- Add more devices to match frontend data
INSERT INTO t_device (device_name, device_type, location, purchase_date, is_loaned, status) VALUES
('流量计', '计量设备', '仓库B', '2024-04-15', FALSE, '正常'),
('湿度计', '传感器', '仓库A', '2024-02-20', FALSE, '正常'),
('微波功率计', '检测设备', '实验室3', '2024-05-10', FALSE, '正常'),
('红外热像仪', '检测设备', '实验室2', '2024-06-01', FALSE, '正常'),
('电流表', '测量工具', '仓库C', '2024-03-15', FALSE, '正常'),
('光学传感器', '传感器', '仓库C', '2024-01-08', FALSE, '维修中'),
('电源模块', '电子设备', '测试台', '2024-02-28', FALSE, '正常');

-- Update some devices to be loaned (simulate current loans)
UPDATE t_device SET is_loaned = TRUE, current_holder_id = 3, loan_date = '2024-11-20' WHERE device_id = 2; -- 压力测试仪 -> 李四
UPDATE t_device SET is_loaned = TRUE, current_holder_id = 6, loan_date = '2024-12-01' WHERE device_id = 5; -- 风速检测仪 -> 孙七 (E005)
UPDATE t_device SET is_loaned = TRUE, current_holder_id = 9, loan_date = '2024-12-05' WHERE device_id = 8; -- 信号发生器 -> 郑十 (E008)
UPDATE t_device SET is_loaned = TRUE, current_holder_id = 12, loan_date = '2024-12-15' WHERE device_id = 12; -- 万用表 -> 卫十三 (E011)

-- Add loan records with various statuses (PENDING, APPROVED, REJECTED) to match frontend
-- PENDING loan applications
INSERT INTO t_loan_record (device_id, applicant_user_id, apply_date, expected_return_date, approval_status, return_status, purpose) VALUES
(1, 2, '2025-11-28 10:00:00', '2025-12-15', 'PENDING', 'NOT_RETURNED', '用于新产品测试'),
(11, 5, '2025-11-25 14:30:00', '2025-12-05', 'PENDING', 'NOT_RETURNED', '临时搭建测试环境'),
(7, 8, '2025-11-10 09:00:00', '2025-12-01', 'PENDING', 'NOT_RETURNED', '高精度信号分析');

-- APPROVED loans (currently active)
INSERT INTO t_loan_record (device_id, applicant_user_id, apply_date, expected_return_date, approval_status, approved_by, approved_at, return_status, purpose) VALUES
(2, 3, '2025-11-27 08:30:00', '2025-12-10', 'APPROVED', 1, '2025-11-27 10:00:00', 'NOT_RETURNED', '例行设备校准'),
(5, 6, '2024-12-01 10:00:00', '2024-12-20', 'APPROVED', 1, '2024-12-01 11:00:00', 'NOT_RETURNED', '风速测量实验'),
(8, 9, '2024-12-05 14:00:00', '2024-12-25', 'APPROVED', 1, '2024-12-05 15:00:00', 'NOT_RETURNED', '信号分析测试'),
(12, 12, '2024-12-15 09:00:00', '2025-01-05', 'APPROVED', 1, '2024-12-15 10:00:00', 'NOT_RETURNED', '日常维修');

-- APPROVED and RETURNED loans (history records - normal returns)
INSERT INTO t_loan_record (device_id, applicant_user_id, apply_date, expected_return_date, approval_status, approved_by, approved_at, actual_return_date, return_status, purpose) VALUES
(1, 2, '2025-11-01 09:00:00', '2025-11-15', 'APPROVED', 1, '2025-11-01 10:00:00', '2025-11-14 16:00:00', 'NORMAL', '产品温度测试'),
(8, 4, '2025-11-05 11:00:00', '2025-11-20', 'APPROVED', 1, '2025-11-05 14:00:00', '2025-11-20 09:00:00', 'NORMAL', '波形信号分析'),
(9, 6, '2025-11-10 10:00:00', '2025-11-25', 'APPROVED', 1, '2025-11-10 11:00:00', '2025-11-24 14:00:00', 'NORMAL', '工程测量'),
(12, 7, '2025-11-15 08:00:00', '2025-12-05', 'APPROVED', 1, '2025-11-15 09:00:00', '2025-12-01 16:00:00', 'NORMAL', '日常维修');

-- APPROVED and RETURNED loans (overdue returns)
INSERT INTO t_loan_record (device_id, applicant_user_id, apply_date, expected_return_date, approval_status, approved_by, approved_at, actual_return_date, return_status, purpose) VALUES
(2, 3, '2025-10-20 09:00:00', '2025-11-10', 'APPROVED', 1, '2025-10-20 11:00:00', '2025-11-15 10:00:00', 'OVERDUE', '压力测试延期'),
(11, 5, '2025-10-15 14:00:00', '2025-11-01', 'APPROVED', 1, '2025-10-15 16:00:00', '2025-11-03 11:00:00', 'OVERDUE', '网络环境搭建'),
(7, 8, '2025-10-01 10:00:00', '2025-10-30', 'APPROVED', 1, '2025-10-01 14:00:00', '2025-11-05 15:00:00', 'OVERDUE', '示波器分析超时');

-- REJECTED loan applications
INSERT INTO t_loan_record (device_id, applicant_user_id, apply_date, expected_return_date, approval_status, approved_by, approved_at, return_status, purpose, rejection_reason) VALUES
(8, 4, '2025-11-26 09:00:00', '2025-12-20', 'REJECTED', 1, '2025-11-26 14:00:00', NULL, '设备类型不匹配', '设备类型不匹配使用需求'),
(19, 9, '2025-11-05 11:00:00', '2025-11-30', 'REJECTED', 1, '2025-11-05 15:00:00', NULL, '需要电源模块', '库存不足');
