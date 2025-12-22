-- devicedb_reset.sql
-- Combined reset script for Device Management System
-- Drops and recreates the database, creates a user, and imports schema + seed data.

-- WARNING: Running this will destroy existing data in `devicedb`.
-- NOTE: Some UPDATE/INSERT statements assume auto-increment IDs based on the insertion order of seed data.
-- If you change the seed rows or insertion order, update the hard-coded IDs in UPDATE/INSERT statements accordingly.

SET FOREIGN_KEY_CHECKS=0;

DROP DATABASE IF EXISTS `devicedb`;
CREATE DATABASE `devicedb` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Using root account only; do not create or grant a separate application user here
-- (User requested to use root/root for database connection)
USE `devicedb`;

-- ==================================================
-- V1__Initial_Schema.sql
-- ==================================================

-- Users table
CREATE TABLE IF NOT EXISTS t_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id VARCHAR(50) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(200) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index on staff_id for faster lookups
CREATE INDEX idx_user_staff_id ON t_user (staff_id);

-- Devices table
CREATE TABLE IF NOT EXISTS t_device (
    device_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(50),
    location VARCHAR(200),
    purchase_date DATE,
    is_loaned BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) DEFAULT '正常',
    current_holder_id BIGINT,
    loan_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (current_holder_id) REFERENCES t_user(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for common queries
CREATE INDEX idx_device_name ON t_device (device_name);
CREATE INDEX idx_device_is_loaned ON t_device (is_loaned);
CREATE INDEX idx_device_status ON t_device (status);

-- Loan records table
CREATE TABLE IF NOT EXISTS t_loan_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    applicant_user_id BIGINT NOT NULL,
    apply_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_return_date DATE NOT NULL,
    actual_return_date TIMESTAMP,
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    return_status VARCHAR(20),
    purpose VARCHAR(500),
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (device_id) REFERENCES t_device(device_id) ON DELETE CASCADE,
    FOREIGN KEY (applicant_user_id) REFERENCES t_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES t_user(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for common queries
CREATE INDEX idx_loan_device_id ON t_loan_record (device_id);
CREATE INDEX idx_loan_applicant_id ON t_loan_record (applicant_user_id);
CREATE INDEX idx_loan_approval_status ON t_loan_record (approval_status);
CREATE INDEX idx_loan_apply_date ON t_loan_record (apply_date);

-- ==================================================
-- V2__Initial_Data.sql
-- ==================================================

-- Insert admin user (password: admin123)
-- BCrypt hash of 'admin123' (same hash used for all initial users)
INSERT INTO t_user (staff_id, username, password, role, status, email, phone) VALUES
('ADMIN001', '管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', 'ACTIVE', NULL, NULL),
('E001', '张三', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', NULL, NULL),
('E002', '李四', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', NULL, NULL),
('E003', '王五', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', NULL, NULL),
('E004', '赵六', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', NULL, NULL);

-- Insert devices
INSERT INTO t_device (device_name, device_type, location, purchase_date, is_loaned, status) VALUES
('温度传感器', '传感器', '仓库A区', '2024-06-01', FALSE, '正常'),
('压力测试仪', '检测设备', '实验室2', '2023-11-12', FALSE, '正常'),
('摄像头模块', '监控设备', '大门口', '2024-01-08', FALSE, '正常'),
('环境监测仪', '传感器', '车间1', '2023-09-14', FALSE, '正常'),
('风速检测仪', '检测设备', '仓库B区', '2024-05-22', FALSE, '正常'),
('电压表', '检测设备', '实验室1', '2022-12-03', FALSE, '故障'),
('示波器', '电子设备', '研发室', '2024-07-15', FALSE, '正常'),
('信号发生器', '电子设备', '测试台', '2023-10-01', FALSE, '正常'),
('激光测距仪', '测量工具', '工具间', '2024-03-20', FALSE, '正常'),
('数据采集卡', '配件', '服务器房', '2024-08-08', FALSE, '正常'),
('网络交换机', '网络设备', '机房', '2023-05-01', FALSE, '正常'),
('万用表', '测量工具', '实验室3', '2024-02-10', FALSE, '正常');

-- ==================================================
-- V3__Additional_Seed_Data.sql
-- ==================================================

-- Add more users to match frontend data
INSERT INTO t_user (staff_id, username, password, role, status, email, phone) VALUES
('E005', '孙七', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', NULL, NULL),
('E006', '周八', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', NULL, NULL),
('E007', '吴九', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', NULL, NULL),
('E008', '郑十', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', NULL, NULL),
('E009', '冯十一', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', NULL, NULL),
('E010', '陈十二', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', NULL, NULL),
('E011', '卫十三', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', NULL, NULL);

-- Add more devices to match frontend data
INSERT INTO t_device (device_name, device_type, location, purchase_date, is_loaned, status) VALUES
('流量计', '计量设备', '仓库B', '2024-04-15', FALSE, '正常'),
('湿度计', '传感器', '仓库A', '2024-02-20', FALSE, '正常'),
('微波功率计', '检测设备', '实验室3', '2024-05-10', FALSE, '正常'),
('红外热像仪', '检测设备', '实验室2', '2024-06-01', FALSE, '正常'),
('电流表', '测量工具', '仓库C', '2024-03-15', FALSE, '正常'),
('光学传感器', '传感器', '仓库C', '2024-01-08', FALSE, '故障'),
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

-- Create dashboard aggregates (materialized single-row summary for homepage)
CREATE TABLE IF NOT EXISTS dashboard_aggregates (
  id INT PRIMARY KEY DEFAULT 1,
  total_devices INT DEFAULT 0,
  loaned_devices INT DEFAULT 0,
  status_normal INT DEFAULT 0,
  status_fault INT DEFAULT 0,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Ensure single row exists
INSERT INTO dashboard_aggregates (id) VALUES (1) ON DUPLICATE KEY UPDATE id = id;

-- Stored procedure to refresh aggregates (can be called manually or by scheduler)
DELIMITER $$
CREATE PROCEDURE sp_refresh_dashboard_aggregates()
BEGIN
  UPDATE dashboard_aggregates SET
    total_devices = (SELECT COUNT(*) FROM t_device),
    loaned_devices = (SELECT COUNT(*) FROM t_device WHERE is_loaned = TRUE),
    status_normal = (SELECT COUNT(*) FROM t_device WHERE status = '正常'),
    status_fault = (SELECT COUNT(*) FROM t_device WHERE status = '故障'),
    updated_at = CURRENT_TIMESTAMP
  WHERE id = 1;
END$$

-- Stored procedure to return device status distribution (uses SQL aggregation)
CREATE PROCEDURE sp_get_status_distribution()
BEGIN
  SELECT status AS device_status, COUNT(*) AS cnt
  FROM t_device
  GROUP BY status;
END$$
DELIMITER ;

-- Initialize aggregates with current data
CALL sp_refresh_dashboard_aggregates();

-- View for device status distribution (for charts)
CREATE OR REPLACE VIEW vw_device_status_distribution AS
SELECT status AS device_status, COUNT(*) AS cnt
FROM t_device
GROUP BY status;

-- Triggers to keep dashboard_aggregates up-to-date on device changes (incremental updates)
DELIMITER $$
CREATE TRIGGER trg_t_device_after_insert
AFTER INSERT ON t_device
FOR EACH ROW
BEGIN
  UPDATE dashboard_aggregates SET total_devices = total_devices + 1 WHERE id = 1;
  IF NEW.is_loaned THEN
    UPDATE dashboard_aggregates SET loaned_devices = loaned_devices + 1 WHERE id = 1;
  END IF;
  IF NEW.status = '正常' THEN
    UPDATE dashboard_aggregates SET status_normal = status_normal + 1 WHERE id = 1;
  ELSEIF NEW.status = '故障' THEN
    UPDATE dashboard_aggregates SET status_fault = status_fault + 1 WHERE id = 1;
  END IF;
END$$

CREATE TRIGGER trg_t_device_after_update
AFTER UPDATE ON t_device
FOR EACH ROW
BEGIN
  -- loaned count change
  IF OLD.is_loaned <> NEW.is_loaned THEN
    IF NEW.is_loaned THEN
      UPDATE dashboard_aggregates SET loaned_devices = loaned_devices + 1 WHERE id = 1;
    ELSE
      UPDATE dashboard_aggregates SET loaned_devices = loaned_devices - 1 WHERE id = 1;
    END IF;
  END IF;

  -- status change adjustments
  IF OLD.status <> NEW.status THEN
    IF OLD.status = '正常' THEN
      UPDATE dashboard_aggregates SET status_normal = status_normal - 1 WHERE id = 1;
    ELSEIF OLD.status = '故障' THEN
      UPDATE dashboard_aggregates SET status_fault = status_fault - 1 WHERE id = 1;
    END IF;

    IF NEW.status = '正常' THEN
      UPDATE dashboard_aggregates SET status_normal = status_normal + 1 WHERE id = 1;
    ELSEIF NEW.status = '故障' THEN
      UPDATE dashboard_aggregates SET status_fault = status_fault + 1 WHERE id = 1;
    END IF;
  END IF;
END$$

CREATE TRIGGER trg_t_device_after_delete
AFTER DELETE ON t_device
FOR EACH ROW
BEGIN
  UPDATE dashboard_aggregates SET total_devices = total_devices - 1 WHERE id = 1;
  IF OLD.is_loaned THEN
    UPDATE dashboard_aggregates SET loaned_devices = loaned_devices - 1 WHERE id = 1;
  END IF;
  IF OLD.status = '正常' THEN
    UPDATE dashboard_aggregates SET status_normal = status_normal - 1 WHERE id = 1;
  ELSEIF OLD.status = '故障' THEN
    UPDATE dashboard_aggregates SET status_fault = status_fault - 1 WHERE id = 1;
  END IF;
END$$
DELIMITER ;

SET FOREIGN_KEY_CHECKS=1;

-- End of reset script
