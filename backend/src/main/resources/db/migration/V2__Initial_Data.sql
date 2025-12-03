-- V2__Initial_Data.sql
-- Device Management System - Initial Test Data

-- Insert admin user (password: admin123)
INSERT INTO t_user (staff_id, username, password, role, status, email, phone) VALUES
('ADMIN001', '管理员', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'ADMIN', 'ACTIVE', 'admin@company.com', '13800000001'),
('E001', '张三', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'USER', 'ACTIVE', 'zhangsan@company.com', '13800000002'),
('E002', '李四', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'USER', 'ACTIVE', 'lisi@company.com', '13800000003'),
('E003', '王五', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'USER', 'ACTIVE', 'wangwu@company.com', '13800000004'),
('E004', '赵六', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'USER', 'ACTIVE', 'zhaoliu@company.com', '13800000005');

-- Insert devices
INSERT INTO t_device (device_name, device_type, location, purchase_date, is_loaned, status) VALUES
('温度传感器', '传感器', '仓库A区', '2024-06-01', FALSE, '正常'),
('压力测试仪', '检测设备', '实验室2', FALSE, '正常'),
('摄像头模块', '监控设备', '大门口', FALSE, '正常'),
('环境监测仪', '传感器', '车间1', FALSE, '正常'),
('风速检测仪', '检测设备', '仓库B区', FALSE, '正常'),
('电压表', '检测设备', '实验室1', FALSE, '故障'),
('示波器', '电子设备', '研发室', FALSE, '正常'),
('信号发生器', '电子设备', '测试台', FALSE, '正常'),
('激光测距仪', '测量工具', '工具间', FALSE, '正常'),
('数据采集卡', '配件', '服务器房', FALSE, '正常'),
('网络交换机', '网络设备', '机房', FALSE, '正常'),
('万用表', '测量工具', '实验室3', FALSE, '正常');
