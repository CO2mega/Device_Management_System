# 设备管理系统 - 后端数据库操作SQL文档

本文档整理了设备管理系统后端所有核心数据库操作的原生SQL语句，用于实验报告。

## 目录
1. [数据库表结构](#数据库表结构)
2. [索引定义](#索引定义)
3. [示例数据](#示例数据)
4. [用户管理操作](#用户管理操作)
5. [设备管理操作](#设备管理操作)
6. [借用记录操作](#借用记录操作)
7. [事务操作](#事务操作)
8. [统计查询操作](#统计查询操作)

---

## 数据库表结构

### 1. 用户表（t_user）
```sql
-- 创建用户表，存储系统用户信息
CREATE TABLE t_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键，自增',
    staff_id VARCHAR(50) NOT NULL UNIQUE COMMENT '工号，唯一标识',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，USER-普通用户',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-激活，LOCKED-锁定',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话号码',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 2. 设备表（t_device）
```sql
-- 创建设备表，存储设备信息及借用状态
CREATE TABLE t_device (
    device_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '设备ID，主键，自增',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(50) COMMENT '设备类型',
    location VARCHAR(200) COMMENT '存放位置',
    purchase_date DATE COMMENT '购买日期',
    is_loaned BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已借出：TRUE-已借出，FALSE-可用',
    status VARCHAR(20) DEFAULT '正常' COMMENT '设备状态：正常、故障、维修中等',
    current_holder_id BIGINT COMMENT '当前持有人ID（外键关联用户表）',
    loan_date DATE COMMENT '借出日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    version BIGINT DEFAULT 0 COMMENT '版本号（用于乐观锁）',
    FOREIGN KEY (current_holder_id) REFERENCES t_user(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';
```

### 3. 借用记录表（t_loan_record）
```sql
-- 创建借用记录表，存储设备借用申请及审批记录
CREATE TABLE t_loan_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID，主键，自增',
    device_id BIGINT NOT NULL COMMENT '设备ID（外键关联设备表）',
    applicant_user_id BIGINT NOT NULL COMMENT '申请人ID（外键关联用户表）',
    apply_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请日期',
    expected_return_date DATE NOT NULL COMMENT '预计归还日期',
    actual_return_date TIMESTAMP COMMENT '实际归还日期',
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态：PENDING-待处理，APPROVED-已批准，REJECTED-已驳回',
    return_status VARCHAR(20) COMMENT '归还状态：NOT_RETURNED-未归还，NORMAL-正常归还，OVERDUE-逾期归还',
    purpose VARCHAR(500) COMMENT '借用用途',
    approved_by BIGINT COMMENT '审批人ID（外键关联用户表）',
    approved_at TIMESTAMP COMMENT '审批时间',
    rejection_reason VARCHAR(500) COMMENT '驳回原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    version BIGINT DEFAULT 0 COMMENT '版本号（用于乐观锁）',
    FOREIGN KEY (device_id) REFERENCES t_device(device_id) ON DELETE CASCADE,
    FOREIGN KEY (applicant_user_id) REFERENCES t_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES t_user(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借用记录表';
```

---

## 索引定义

### 用户表索引
```sql
-- 在工号字段创建索引，加速按工号查询
CREATE INDEX idx_user_staff_id ON t_user(staff_id);
```

### 设备表索引
```sql
-- 在设备名称字段创建索引，加速按名称查询
CREATE INDEX idx_device_name ON t_device(device_name);

-- 在借用状态字段创建索引，加速按借用状态筛选
CREATE INDEX idx_device_is_loaned ON t_device(is_loaned);

-- 在设备状态字段创建索引，加速按状态筛选
CREATE INDEX idx_device_status ON t_device(status);
```

### 借用记录表索引
```sql
-- 在设备ID字段创建索引，加速查询特定设备的借用记录
CREATE INDEX idx_loan_device_id ON t_loan_record(device_id);

-- 在申请人ID字段创建索引，加速查询特定用户的借用记录
CREATE INDEX idx_loan_applicant_id ON t_loan_record(applicant_user_id);

-- 在审批状态字段创建索引，加速按审批状态筛选
CREATE INDEX idx_loan_approval_status ON t_loan_record(approval_status);

-- 在申请日期字段创建索引，加速按日期排序和范围查询
CREATE INDEX idx_loan_apply_date ON t_loan_record(apply_date);
```

---

## 示例数据

### 插入测试用户
```sql
-- 插入管理员和普通用户（密码：admin123，使用BCrypt加密）
-- 注意：实际应用中密码必须由应用程序使用BCrypt算法加密后再插入
INSERT INTO t_user (staff_id, username, password, role, status, email, phone) VALUES
('ADMIN001', '管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', 'ACTIVE', 'admin@company.com', '13800000001'),
('E001', '张三', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'zhangsan@company.com', '13800000002'),
('E002', '李四', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 'ACTIVE', 'lisi@company.com', '13800000003');
```

### 插入测试设备
```sql
-- 插入各类设备数据
INSERT INTO t_device (device_name, device_type, location, purchase_date, is_loaned, status) VALUES
('温度传感器', '传感器', '仓库A区', '2024-06-01', FALSE, '正常'),
('压力测试仪', '检测设备', '实验室2', '2023-11-12', FALSE, '正常'),
('摄像头模块', '监控设备', '大门口', '2024-01-08', FALSE, '正常'),
('示波器', '电子设备', '研发室', '2024-07-15', FALSE, '正常');
```

---

## 用户管理操作

### 1. 查询用户

#### 根据用户ID查询
```sql
-- 根据用户ID查询用户详细信息
SELECT user_id, staff_id, username, role, status, email, phone, created_at, updated_at
FROM t_user
WHERE user_id = ?;
```

#### 根据工号查询
```sql
-- 根据工号查询用户（用于登录验证）
SELECT user_id, staff_id, username, password, role, status, email, phone, created_at, updated_at
FROM t_user
WHERE staff_id = ?;
```

#### 根据用户名查询
```sql
-- 根据用户名查询用户
SELECT user_id, staff_id, username, password, role, status, email, phone, created_at, updated_at
FROM t_user
WHERE username = ?;
```

#### 条件筛选查询（支持分页）
```sql
-- 多条件筛选查询用户列表（支持搜索、角色筛选、状态筛选）
SELECT user_id, staff_id, username, role, status, email, phone, created_at, updated_at
FROM t_user
WHERE (? IS NULL OR staff_id LIKE CONCAT('%', ?, '%') OR username LIKE CONCAT('%', ?, '%'))
  AND (? IS NULL OR role = ?)
  AND (? IS NULL OR status = ?)
ORDER BY created_at DESC
LIMIT ? OFFSET ?;
```

#### 统计角色数量
```sql
-- 统计特定角色的用户数量
SELECT COUNT(*) FROM t_user WHERE role = ?;
```

#### 检查工号是否存在
```sql
-- 检查工号是否已存在（用于创建用户时的唯一性验证）
SELECT COUNT(*) FROM t_user WHERE staff_id = ?;
```

```sql
-- 检查工号是否已被其他用户使用（用于更新用户时的唯一性验证）
SELECT COUNT(*) FROM t_user WHERE staff_id = ? AND user_id != ?;
```

### 2. 创建用户
```sql
-- 创建新用户（密码需由应用程序加密后传入）
INSERT INTO t_user (staff_id, username, password, role, status, email, phone, created_at, updated_at)
VALUES (?, ?, ?, ?, 'ACTIVE', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

### 3. 更新用户
```sql
-- 更新用户基本信息
UPDATE t_user
SET staff_id = ?,
    username = ?,
    role = ?,
    status = ?,
    email = ?,
    phone = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = ?;
```

```sql
-- 更新用户密码
UPDATE t_user
SET password = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = ?;
```

### 4. 删除用户
```sql
-- 物理删除用户（实际应用中建议使用逻辑删除）
DELETE FROM t_user WHERE user_id = ?;
```

```sql
-- 逻辑删除用户（推荐）
UPDATE t_user
SET status = 'INACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = ?;
```

---

## 设备管理操作

### 1. 查询设备

#### 根据设备ID查询
```sql
-- 根据设备ID查询设备详细信息（包含当前持有人信息）
SELECT d.device_id, d.device_name, d.device_type, d.location, d.purchase_date,
       d.is_loaned, d.status, d.loan_date, d.current_holder_id,
       u.username AS current_holder_name,
       d.created_at, d.updated_at, d.version
FROM t_device d
LEFT JOIN t_user u ON d.current_holder_id = u.user_id
WHERE d.device_id = ?;
```

#### 条件筛选查询（支持分页）
```sql
-- 多条件筛选查询设备列表
SELECT d.device_id, d.device_name, d.device_type, d.location, d.purchase_date,
       d.is_loaned, d.status, d.loan_date, d.current_holder_id,
       u.username AS current_holder_name,
       d.created_at, d.updated_at
FROM t_device d
LEFT JOIN t_user u ON d.current_holder_id = u.user_id
WHERE (? IS NULL OR d.device_name LIKE CONCAT('%', ?, '%') 
       OR d.device_type LIKE CONCAT('%', ?, '%') 
       OR d.location LIKE CONCAT('%', ?, '%'))
  AND (? IS NULL OR d.status = ?)
  AND (? IS NULL OR d.is_loaned = ?)
ORDER BY d.created_at DESC
LIMIT ? OFFSET ?;
```

#### 悲观锁查询设备
```sql
-- 使用悲观锁查询设备（用于防止并发借用冲突）
SELECT device_id, device_name, device_type, location, is_loaned, status, 
       current_holder_id, loan_date, version
FROM t_device
WHERE device_id = ?
FOR UPDATE;
```

#### 统计设备数量
```sql
-- 统计总设备数
SELECT COUNT(*) FROM t_device;
```

```sql
-- 统计已借出的设备数
SELECT COUNT(*) FROM t_device WHERE is_loaned = TRUE;
```

```sql
-- 统计可用设备数
SELECT COUNT(*) FROM t_device WHERE is_loaned = FALSE;
```

#### 检查设备名称重复
```sql
-- 检查设备名称是否已被其他设备使用（用于更新时的验证）
SELECT COUNT(*) FROM t_device WHERE device_name = ? AND device_id != ?;
```

### 2. 创建设备
```sql
-- 创建新设备
INSERT INTO t_device (device_name, device_type, location, purchase_date, is_loaned, status, created_at, updated_at, version)
VALUES (?, ?, ?, ?, FALSE, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
```

### 3. 更新设备
```sql
-- 更新设备基本信息
UPDATE t_device
SET device_name = ?,
    device_type = ?,
    location = ?,
    purchase_date = ?,
    status = ?,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE device_id = ?;
```

```sql
-- 更新设备借用状态（借出）
UPDATE t_device
SET is_loaned = TRUE,
    current_holder_id = ?,
    loan_date = CURRENT_DATE,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE device_id = ?;
```

```sql
-- 更新设备借用状态（归还）
UPDATE t_device
SET is_loaned = FALSE,
    current_holder_id = NULL,
    loan_date = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE device_id = ?;
```

### 4. 删除设备
```sql
-- 检查设备是否有未归还的借用记录
SELECT COUNT(*) 
FROM t_loan_record
WHERE device_id = ? 
  AND approval_status = 'APPROVED' 
  AND actual_return_date IS NULL;
```

```sql
-- 物理删除设备（确认无未归还借用后）
DELETE FROM t_device WHERE device_id = ?;
```

---

## 借用记录操作

### 1. 查询借用记录

#### 根据记录ID查询
```sql
-- 根据记录ID查询借用记录详细信息
SELECT lr.record_id, lr.device_id, lr.applicant_user_id, lr.apply_date,
       lr.expected_return_date, lr.actual_return_date,
       lr.approval_status, lr.return_status, lr.purpose,
       lr.approved_by, lr.approved_at, lr.rejection_reason,
       lr.created_at, lr.updated_at,
       d.device_name,
       a.username AS applicant_name, a.staff_id AS applicant_staff_id,
       ap.username AS approved_by_name
FROM t_loan_record lr
INNER JOIN t_device d ON lr.device_id = d.device_id
INNER JOIN t_user a ON lr.applicant_user_id = a.user_id
LEFT JOIN t_user ap ON lr.approved_by = ap.user_id
WHERE lr.record_id = ?;
```

#### 条件筛选查询（支持分页）
```sql
-- 多条件筛选查询借用记录列表
SELECT lr.record_id, lr.device_id, lr.applicant_user_id, lr.apply_date,
       lr.expected_return_date, lr.actual_return_date,
       lr.approval_status, lr.return_status, lr.purpose,
       lr.approved_at, lr.rejection_reason, lr.created_at, lr.updated_at,
       d.device_name,
       a.username AS applicant_name, a.staff_id AS applicant_staff_id,
       ap.username AS approved_by_name
FROM t_loan_record lr
INNER JOIN t_device d ON lr.device_id = d.device_id
INNER JOIN t_user a ON lr.applicant_user_id = a.user_id
LEFT JOIN t_user ap ON lr.approved_by = ap.user_id
WHERE (? IS NULL OR d.device_name LIKE CONCAT('%', ?, '%') OR a.username LIKE CONCAT('%', ?, '%'))
  AND (? IS NULL OR lr.approval_status = ?)
ORDER BY lr.apply_date DESC
LIMIT ? OFFSET ?;
```

#### 查询设备的活跃借用记录
```sql
-- 查询特定设备的未归还借用记录
SELECT record_id, device_id, applicant_user_id, apply_date, expected_return_date,
       approval_status, return_status, purpose
FROM t_loan_record
WHERE device_id = ? 
  AND approval_status = 'APPROVED' 
  AND actual_return_date IS NULL;
```

#### 查询用户的活跃借用记录
```sql
-- 查询特定用户的未归还借用记录
SELECT record_id, device_id, applicant_user_id, apply_date, expected_return_date,
       approval_status, return_status, purpose
FROM t_loan_record
WHERE applicant_user_id = ? 
  AND actual_return_date IS NULL;
```

#### 统计借用记录
```sql
-- 统计待处理的借用申请数
SELECT COUNT(*) FROM t_loan_record WHERE approval_status = 'PENDING';
```

```sql
-- 统计已批准的借用记录数
SELECT COUNT(*) FROM t_loan_record WHERE approval_status = 'APPROVED';
```

```sql
-- 统计已驳回的借用记录数
SELECT COUNT(*) FROM t_loan_record WHERE approval_status = 'REJECTED';
```

```sql
-- 统计当前活跃借用数（已批准且未归还）
SELECT COUNT(*) 
FROM t_loan_record
WHERE approval_status = 'APPROVED' 
  AND actual_return_date IS NULL;
```

#### 检查设备是否有未归还借用
```sql
-- 检查设备是否有未归还的已批准借用
SELECT COUNT(*) 
FROM t_loan_record
WHERE device_id = ? 
  AND approval_status = 'APPROVED' 
  AND actual_return_date IS NULL;
```

### 2. 创建借用申请
```sql
-- 创建新的借用申请
INSERT INTO t_loan_record (device_id, applicant_user_id, apply_date, expected_return_date, 
                           approval_status, return_status, purpose, created_at, updated_at, version)
VALUES (?, ?, CURRENT_TIMESTAMP, ?, 'PENDING', 'NOT_RETURNED', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
```

### 3. 更新借用记录

#### 批准借用申请
```sql
-- 批准借用申请，更新审批信息
UPDATE t_loan_record
SET approval_status = 'APPROVED',
    approved_by = ?,
    approved_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE record_id = ?;
```

#### 驳回借用申请
```sql
-- 驳回借用申请，记录驳回原因
UPDATE t_loan_record
SET approval_status = 'REJECTED',
    approved_by = ?,
    approved_at = CURRENT_TIMESTAMP,
    rejection_reason = ?,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE record_id = ?;
```

#### 归还设备
```sql
-- 记录设备归还，更新归还时间和状态
UPDATE t_loan_record
SET actual_return_date = CURRENT_TIMESTAMP,
    return_status = CASE
        WHEN CURRENT_DATE > expected_return_date THEN 'OVERDUE'
        ELSE 'NORMAL'
    END,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE record_id = ?;
```

---

## 事务操作

### 1. 借用申请事务
```sql
-- 事务：创建借用申请
-- 说明：使用悲观锁确保设备不会被多人同时借用

START TRANSACTION;

-- 1. 使用悲观锁查询设备状态
SELECT device_id, device_name, is_loaned, status, version
FROM t_device
WHERE device_id = ?
FOR UPDATE;

-- 2. 检查设备是否已借出（应用层验证）
-- 如果 is_loaned = TRUE，则抛出异常并回滚

-- 3. 检查是否有未归还的已批准借用记录
SELECT COUNT(*) 
FROM t_loan_record
WHERE device_id = ? 
  AND approval_status = 'APPROVED' 
  AND actual_return_date IS NULL;
-- 如果 COUNT > 0，则抛出异常并回滚

-- 4. 创建借用申请记录
INSERT INTO t_loan_record (device_id, applicant_user_id, apply_date, expected_return_date, 
                           approval_status, return_status, purpose, created_at, updated_at, version)
VALUES (?, ?, CURRENT_TIMESTAMP, ?, 'PENDING', 'NOT_RETURNED', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

COMMIT;
-- 如果任何步骤失败，则执行 ROLLBACK;
```

### 2. 批准借用事务
```sql
-- 事务：批准借用申请，更新设备和借用记录
-- 说明：确保设备状态和借用记录的原子性更新

START TRANSACTION;

-- 1. 查询借用记录详情
SELECT record_id, device_id, applicant_user_id, approval_status
FROM t_loan_record
WHERE record_id = ?;

-- 2. 验证借用记录状态为PENDING（应用层验证）
-- 如果 approval_status != 'PENDING'，则抛出异常并回滚

-- 3. 使用悲观锁查询设备状态
SELECT device_id, is_loaned, version
FROM t_device
WHERE device_id = ?
FOR UPDATE;

-- 4. 验证设备未被借出（应用层验证）
-- 如果 is_loaned = TRUE，则抛出异常并回滚

-- 5. 更新设备状态为已借出
UPDATE t_device
SET is_loaned = TRUE,
    current_holder_id = ?,
    loan_date = CURRENT_DATE,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE device_id = ?;

-- 6. 更新借用记录为已批准
UPDATE t_loan_record
SET approval_status = 'APPROVED',
    approved_by = ?,
    approved_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE record_id = ?;

COMMIT;
-- 如果任何步骤失败，则执行 ROLLBACK;
```

### 3. 归还设备事务
```sql
-- 事务：归还设备，更新设备和借用记录
-- 说明：确保设备状态和借用记录的原子性更新

START TRANSACTION;

-- 1. 查询借用记录详情
SELECT record_id, device_id, approval_status, actual_return_date, expected_return_date
FROM t_loan_record
WHERE record_id = ?;

-- 2. 验证借用记录状态（应用层验证）
-- 如果 approval_status != 'APPROVED' 或 actual_return_date IS NOT NULL，则抛出异常并回滚

-- 3. 使用悲观锁查询设备
SELECT device_id, is_loaned, version
FROM t_device
WHERE device_id = ?
FOR UPDATE;

-- 4. 更新设备状态为可用
UPDATE t_device
SET is_loaned = FALSE,
    current_holder_id = NULL,
    loan_date = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE device_id = ?;

-- 5. 更新借用记录归还信息
UPDATE t_loan_record
SET actual_return_date = CURRENT_TIMESTAMP,
    return_status = CASE
        WHEN CURRENT_DATE > expected_return_date THEN 'OVERDUE'
        ELSE 'NORMAL'
    END,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE record_id = ?;

COMMIT;
-- 如果任何步骤失败，则执行 ROLLBACK;
```

### 4. 驳回借用申请事务
```sql
-- 事务：驳回借用申请
-- 说明：简单的状态更新，不涉及设备状态变更

START TRANSACTION;

-- 1. 查询借用记录
SELECT record_id, approval_status
FROM t_loan_record
WHERE record_id = ?;

-- 2. 验证借用记录状态为PENDING（应用层验证）
-- 如果 approval_status != 'PENDING'，则抛出异常并回滚

-- 3. 更新借用记录为已驳回
UPDATE t_loan_record
SET approval_status = 'REJECTED',
    approved_by = ?,
    approved_at = CURRENT_TIMESTAMP,
    rejection_reason = ?,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE record_id = ?;

COMMIT;
-- 如果任何步骤失败，则执行 ROLLBACK;
```

---

## 统计查询操作

### 1. 仪表板统计
```sql
-- 统计设备总数
SELECT COUNT(*) AS total_devices FROM t_device;
```

```sql
-- 统计已借出设备数
SELECT COUNT(*) AS loaned_devices 
FROM t_device 
WHERE is_loaned = TRUE;
```

```sql
-- 统计可用设备数
SELECT COUNT(*) AS available_devices 
FROM t_device 
WHERE is_loaned = FALSE;
```

```sql
-- 统计用户总数
SELECT COUNT(*) AS total_users FROM t_user;
```

```sql
-- 统计待处理借用申请数
SELECT COUNT(*) AS pending_loans 
FROM t_loan_record 
WHERE approval_status = 'PENDING';
```

```sql
-- 统计已批准借用数
SELECT COUNT(*) AS approved_loans 
FROM t_loan_record 
WHERE approval_status = 'APPROVED';
```

```sql
-- 统计已驳回借用数
SELECT COUNT(*) AS rejected_loans 
FROM t_loan_record 
WHERE approval_status = 'REJECTED';
```

```sql
-- 统计当前活跃借用数（未归还）
SELECT COUNT(*) AS active_loans 
FROM t_loan_record 
WHERE approval_status = 'APPROVED' 
  AND actual_return_date IS NULL;
```

### 2. 综合统计查询
```sql
-- 一次性获取所有仪表板统计数据
SELECT 
    (SELECT COUNT(*) FROM t_device) AS total_devices,
    (SELECT COUNT(*) FROM t_device WHERE is_loaned = TRUE) AS loaned_devices,
    (SELECT COUNT(*) FROM t_device WHERE is_loaned = FALSE) AS available_devices,
    (SELECT COUNT(*) FROM t_user) AS total_users,
    (SELECT COUNT(*) FROM t_loan_record WHERE approval_status = 'PENDING') AS pending_loans,
    (SELECT COUNT(*) FROM t_loan_record WHERE approval_status = 'APPROVED') AS approved_loans,
    (SELECT COUNT(*) FROM t_loan_record WHERE approval_status = 'REJECTED') AS rejected_loans,
    (SELECT COUNT(*) FROM t_loan_record WHERE approval_status = 'APPROVED' AND actual_return_date IS NULL) AS active_loans;
```

### 3. 按设备类型统计
```sql
-- 统计各类型设备数量
SELECT device_type, COUNT(*) AS count
FROM t_device
GROUP BY device_type
ORDER BY count DESC;
```

### 4. 按月统计借用记录
```sql
-- 统计每月的借用申请数量
SELECT 
    DATE_FORMAT(apply_date, '%Y-%m') AS month,
    COUNT(*) AS total_applications,
    SUM(CASE WHEN approval_status = 'APPROVED' THEN 1 ELSE 0 END) AS approved,
    SUM(CASE WHEN approval_status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected,
    SUM(CASE WHEN approval_status = 'PENDING' THEN 1 ELSE 0 END) AS pending
FROM t_loan_record
GROUP BY DATE_FORMAT(apply_date, '%Y-%m')
ORDER BY month DESC;
```

### 5. 逾期借用统计
```sql
-- 查询所有逾期未归还的借用记录
SELECT lr.record_id, lr.device_id, d.device_name,
       lr.applicant_user_id, u.username AS applicant_name,
       lr.expected_return_date,
       DATEDIFF(CURRENT_DATE, lr.expected_return_date) AS overdue_days
FROM t_loan_record lr
INNER JOIN t_device d ON lr.device_id = d.device_id
INNER JOIN t_user u ON lr.applicant_user_id = u.user_id
WHERE lr.approval_status = 'APPROVED'
  AND lr.actual_return_date IS NULL
  AND lr.expected_return_date < CURRENT_DATE
ORDER BY overdue_days DESC;
```

---

## 附录：SQL语句说明

### JPA注解到SQL的映射关系

1. **@Entity** → CREATE TABLE
2. **@Id + @GeneratedValue(strategy = GenerationType.IDENTITY)** → PRIMARY KEY AUTO_INCREMENT
3. **@Column** → 表字段定义
4. **@PrePersist** → created_at DEFAULT CURRENT_TIMESTAMP
5. **@PreUpdate** → updated_at 在UPDATE语句中设置为CURRENT_TIMESTAMP
6. **@ManyToOne + @JoinColumn** → FOREIGN KEY约束
7. **@Version** → 乐观锁版本控制（version字段）
8. **@Lock(LockModeType.PESSIMISTIC_WRITE)** → SELECT ... FOR UPDATE（悲观锁）
9. **@Transactional** → START TRANSACTION ... COMMIT/ROLLBACK

### 数据库设计要点

1. **主键策略**：所有表使用BIGINT类型的自增主键
2. **时间戳**：created_at和updated_at自动维护创建和更新时间
3. **外键约束**：
   - ON DELETE CASCADE：级联删除（如借用记录随设备删除）
   - ON DELETE SET NULL：设置为NULL（如设备持有人删除后设为NULL）
4. **索引优化**：在常用查询字段（如状态、日期、外键）上建立索引
5. **并发控制**：
   - 乐观锁：使用version字段，更新时检查版本号
   - 悲观锁：使用FOR UPDATE锁定行，防止并发修改
6. **枚举类型**：使用VARCHAR存储枚举值（如审批状态、归还状态）
7. **逻辑删除**：推荐使用status字段标记删除，而非物理删除

### 事务隔离级别建议

- **READ COMMITTED**：适用于大多数操作，防止脏读
- 关键操作（如借用审批）使用悲观锁（FOR UPDATE）确保数据一致性

### 性能优化建议

1. 对高频查询字段建立索引
2. 使用LIMIT和OFFSET实现分页
3. 避免SELECT *，明确指定需要的字段
4. 复杂查询使用JOIN代替多次查询
5. 统计查询可考虑使用物化视图或定时任务缓存结果

---

**文档版本**：1.0  
**创建日期**：2024  
**适用系统**：设备管理系统（Device Management System）  
**数据库**：MySQL 8.0+
