-- V2__Initial_Data.sql
-- Device Management System - Initial Test Data

-- Insert admin user (password: admin123)
-- BCrypt hash of 'admin123' 
INSERT INTO t_user (staff_id, username, password, role, status, email, phone) VALUES
('ADMIN001', '管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', 'ACTIVE', 'admin@company.com', '13800000001'),
('E001', '张三', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'zhangsan@company.com', '13800000002'),
('E002', '李四', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'lisi@company.com', '13800000003'),
('E003', '王五', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'wangwu@company.com', '13800000004'),
('E004', '赵六', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'zhaoliu@company.com', '13800000005');

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
