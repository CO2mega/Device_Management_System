# 设备管理系统 - 数据库操作（按类别合并、基于项目事实）

目的
- 本文档基于项目当前实现（Java 源码与 SQL 初始化脚本），按类别（增删改查、主/外键、索引、视图、存储过程、触发器、备份、并发）汇总数据库相关的原生 SQL 操作与对应的实现位置。仅陈述项目中已有的事实实现并列出 SQL 示例，便于写实验报告时直接引用。
- 代码/脚本位置示例均以项目根 `backend/` 为基准，例如 `backend/src/main/java/...` 与 `backend/db/devicedb_reset.sql`。

目录（按类别）
1. 增删改查（CRUD）
2. 主键 / 外键（PK / FK）
3. 索引（Indexes）
4. 视图（Views）
5. 存储过程（Stored Procedures）
6. 触发器（Triggers）
7. 备份（Backup）
8. 并发（Locks / 并发控制）

---

## 表说明（每个表的作用 — 快速参考）

下面按表名列出本项目中使用的主要数据库表及其在系统中的职责，方便在写报告时快速说明数据模型的意图。每个条目同时列出与该表相关的视图或用于聚合的逻辑对象（若存在），并标注视图定义的位置（`backend/db/devicedb_reset.sql`）。

- `t_user`：用户表，存储系统用户的账号信息（工号、用户名、密码哈希）、角色和状态；用于登录认证、权限判断和借用记录的申请人/审批人关系。与该表直接相关的主要对象为用户实体/索引（见索引节），没有专门为用户创建的视图。

- `t_device`：设备表，记录设备的基础信息（名称、类型、存放位置、购入日期）、当前借用状态（是否借出、当前持有人）、设备状态（正常/故障）及用于并发控制的版本号等。与 `t_device` 相关的视图/聚合包括：
  - `vw_device_status_distribution`（定义于 `backend/db/devicedb_reset.sql`）：按设备 `status` 聚合计数，常用于仪表盘右侧的状态饼图或后端存储过程的数据源。
  - 其它按 `device_type` 或类型分布的统计可通过 SQL 聚合或视图实现（若脚本中定义，位于 `devicedb_reset.sql`）。

- `t_loan_record`：借用记录表，保存每次设备借用申请、审批与归还的完整流程信息（申请人、申请时间、预计归还、实际归还、审批状态、归还状态、审批人等），是连接 `t_user` 与 `t_device` 的核心交易表。该表的统计通常以聚合查询或在 `dashboard_aggregates` 中缓存，不单独创建针对单表的视图（项目中统计由存储过程或视图组合生成）。

- `dashboard_aggregates`：仪表盘汇总表（单行物化表），用于缓存与加速首页统计数据（设备总数、已借出数、状态分布等），并由触发器或存储过程维护/刷新以减少实时聚合开销。该表与下列对象配合使用：
  - 存储过程 `sp_refresh_dashboard_aggregates()`（定义于 `backend/db/devicedb_reset.sql`）用于初始化/刷新该表。
  - 视图（例如 `vw_device_status_distribution`）或存储过程（例如 `sp_get_status_distribution()`）可作为数据来源，用于填充或比对 `dashboard_aggregates` 的字段值。

- 视图（汇总）：项目中的视图（例如 `vw_device_status_distribution`）被整合到相关表的说明中（以上已标注），视图定义与位置同样在 `backend/db/devicedb_reset.sql` 中。

---

1) 增删改查（CRUD）

说明：本节按对象（User / Device / LoanRecord / Dashboard 相关聚合）分别列出项目中实现 CRUD 的文件位置与对应的原生 SQL 操作示例（来自 `devicedb_reset.sql` 与文档示例），以便在实验报告中说明实际数据库交互。

A. 用户（User / 表：`t_user`）
- 实现位置（Java）:
  - 实体: `backend/src/main/java/com/dms/entity/User.java`
  - 控制器: `backend/src/main/java/com/dms/controller/UserController.java`
  - 服务: `backend/src/main/java/com/dms/service/UserService.java`
  - 仓库: `backend/src/main/java/com/dms/repository/UserRepository.java`
- 典型 SQL 操作（项目中事实）：
  - 创建用户（INSERT）
    ```sql
    INSERT INTO t_user (staff_id, username, password, role, status, email, phone, created_at, updated_at)
    VALUES (?, ?, ?, ?, 'ACTIVE', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
    ```
    - 说明：密码由应用程序使用 BCrypt 加密后存入（Java 层负责加密）。
  - 查询用户（SELECT）
    ```sql
    SELECT user_id, staff_id, username, role, status, email, phone, created_at, updated_at
    FROM t_user
    WHERE staff_id = ?;
    ```
    - 用途：登录验证、按工号查用户等，Repository 层提供方法。
  - 更新用户（UPDATE）
    ```sql
    UPDATE t_user
    SET staff_id = ?, username = ?, role = ?, status = ?, email = ?, phone = ?, updated_at = CURRENT_TIMESTAMP
    WHERE user_id = ?;
    ```
  - 删除用户（DELETE / 逻辑删除）
    ```sql
    -- 物理删除
    DELETE FROM t_user WHERE user_id = ?;

    -- 逻辑删除（更常用）
    UPDATE t_user SET status = 'INACTIVE', updated_at = CURRENT_TIMESTAMP WHERE user_id = ?;
    ```

B. 设备（Device / 表：`t_device`）
- 实现位置（Java）:
  - 实体: `backend/src/main/java/com/dms/entity/Device.java`
  - 控制器: `backend/src/main/java/com/dms/controller/DeviceController.java`
  - 服务: `backend/src/main/java/com/dms/service/DeviceService.java`
  - 仓库: `backend/src/main/java/com/dms/repository/DeviceRepository.java`
- 典型 SQL 操作（项目中事实）：
  - 创建设备（INSERT）
    ```sql
    INSERT INTO t_device (device_name, device_type, location, purchase_date, is_loaned, status, created_at, updated_at, version)
    VALUES (?, ?, ?, ?, FALSE, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
    ```
  - 查询设备（按 ID）
    ```sql
    SELECT d.device_id, d.device_name, d.device_type, d.location, d.purchase_date,
           d.is_loaned, d.status, d.loan_date, d.current_holder_id,
           u.username AS current_holder_name,
           d.created_at, d.updated_at, d.version
    FROM t_device d
    LEFT JOIN t_user u ON d.current_holder_id = u.user_id
    WHERE d.device_id = ?;
    ```
  - 条件筛选（分页）示例
    ```sql
    SELECT d.device_id, d.device_name, d.device_type, d.location, d.purchase_date,
           d.is_loaned, d.status, d.loan_date, d.current_holder_id,
           u.username AS current_holder_name,
           d.created_at, d.updated_at
    FROM t_device d
    LEFT JOIN t_user u ON d.current_holder_id = u.user_id
    WHERE (? IS NULL OR d.device_name LIKE CONCAT('%', ?, '%') OR d.device_type LIKE CONCAT('%', ?, '%') OR d.location LIKE CONCAT('%', ?, '%'))
      AND (? IS NULL OR d.status = ?)
      AND (? IS NULL OR d.is_loaned = ?)
    ORDER BY d.created_at DESC
    LIMIT ? OFFSET ?;
    ```
  - 更新设备（UPDATE）
    ```sql
    UPDATE t_device
    SET device_name = ?, device_type = ?, location = ?, purchase_date = ?, status = ?, updated_at = CURRENT_TIMESTAMP, version = version + 1
    WHERE device_id = ?;
    ```
  - 删除设备（先检查是否有未归还借用）
    ```sql
    SELECT COUNT(*) FROM t_loan_record WHERE device_id = ? AND approval_status = 'APPROVED' AND actual_return_date IS NULL;
    -- 若为0则删除：
    DELETE FROM t_device WHERE device_id = ?;
    ```

C. 借用记录（LoanRecord / 表：`t_loan_record`）
- 实现位置（Java）:
  - 实体: `backend/src/main/java/com/dms/entity/LoanRecord.java`
  - 控制器: `backend/src/main/java/com/dms/controller/LoanRecordController.java`
  - 服务: `backend/src/main/java/com/dms/service/LoanRecordService.java`
  - 仓库: `backend/src/main/java/com/dms/repository/LoanRecordRepository.java`
- 典型 SQL 操作（项目中事实）：
  - 创建借用申请（INSERT）
    ```sql
    INSERT INTO t_loan_record (device_id, applicant_user_id, apply_date, expected_return_date, approval_status, return_status, purpose, created_at, updated_at, version)
    VALUES (?, ?, CURRENT_TIMESTAMP, ?, 'PENDING', 'NOT_RETURNED', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
    ```
  - 查询借用记录（带关联设备和用户）
    ```sql
    SELECT lr.record_id, lr.device_id, lr.applicant_user_id, lr.apply_date, lr.expected_return_date, lr.actual_return_date,
           lr.approval_status, lr.return_status, lr.purpose, lr.approved_by, lr.approved_at, lr.rejection_reason, lr.created_at, lr.updated_at,
           d.device_name, a.username AS applicant_name, a.staff_id AS applicant_staff_id, ap.username AS approved_by_name
    FROM t_loan_record lr
    INNER JOIN t_device d ON lr.device_id = d.device_id
    INNER JOIN t_user a ON lr.applicant_user_id = a.user_id
    LEFT JOIN t_user ap ON lr.approved_by = ap.user_id
    WHERE lr.record_id = ?;
    ```
  - 批准借用（UPDATE）——在事务中更新设备与借用记录（见并发/事务节）
    ```sql
    UPDATE t_device SET is_loaned = TRUE, current_holder_id = ?, loan_date = CURRENT_DATE, updated_at = CURRENT_TIMESTAMP, version = version + 1 WHERE device_id = ?;

    UPDATE t_loan_record SET approval_status = 'APPROVED', approved_by = ?, approved_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP, version = version + 1 WHERE record_id = ?;
    ```
  - 驳回借用（UPDATE）
    ```sql
    UPDATE t_loan_record SET approval_status = 'REJECTED', approved_by = ?, approved_at = CURRENT_TIMESTAMP, rejection_reason = ?, updated_at = CURRENT_TIMESTAMP, version = version + 1 WHERE record_id = ?;
    ```
  - 归还设备（UPDATE）
    ```sql
    UPDATE t_device SET is_loaned = FALSE, current_holder_id = NULL, loan_date = NULL, updated_at = CURRENT_TIMESTAMP, version = version + 1 WHERE device_id = ?;

    UPDATE t_loan_record SET actual_return_date = CURRENT_TIMESTAMP, return_status = CASE WHEN CURRENT_DATE > expected_return_date THEN 'OVERDUE' ELSE 'NORMAL' END, updated_at = CURRENT_TIMESTAMP, version = version + 1 WHERE record_id = ?;
    ```

D. 仪表盘聚合 / 聚合数据（Dashboard）
- 实现位置：
  - 后端服务: `backend/src/main/java/com/dms/service/DashboardService.java`
  - Controller: `backend/src/main/java/com/dms/controller/DashboardController.java`
  - 聚合表/视图/存储过程定义: `backend/db/devicedb_reset.sql`
- 典型 SQL 操作（聚合/统计）
  - 单次综合统计（示例）
    ```sql
    SELECT
      (SELECT COUNT(*) FROM t_device) AS total_devices,
      (SELECT COUNT(*) FROM t_device WHERE is_loaned = TRUE) AS loaned_devices,
      (SELECT COUNT(*) FROM t_device WHERE is_loaned = FALSE) AS available_devices,
      (SELECT COUNT(*) FROM t_user) AS total_users,
      (SELECT COUNT(*) FROM t_loan_record WHERE approval_status = 'PENDING') AS pending_loans,
      (SELECT COUNT(*) FROM t_loan_record WHERE approval_status = 'APPROVED') AS approved_loans,
      (SELECT COUNT(*) FROM t_loan_record WHERE approval_status = 'REJECTED') AS rejected_loans;
    ```
  - 状态分布（备用视图/存储过程可用）：`vw_device_status_distribution` 或 `sp_get_status_distribution()`（见视图/存储过程节）。

---

2) 主键 / 外键（PK / FK）

说明：列出每个表的主/外键定义位置与 Java 实体对应关系（基于项目事实）。

- `t_user`:
  - 主键: `user_id` (`User.java` 中 `@Id` + `@GeneratedValue`)，建表定义在 `backend/db/devicedb_reset.sql`。
- `t_device`:
  - 主键: `device_id` (`Device.java` 中 `@Id`)。
  - 外键: `current_holder_id` REFERENCES `t_user(user_id)`（SQL 脚本中声明，实体映射为 `@ManyToOne` + `@JoinColumn(name = "current_holder_id")`）。
- `t_loan_record`:
  - 主键: `record_id` (`LoanRecord.java` 中 `@Id`)。
  - 外键:
    - `device_id` -> `t_device(device_id)`（`@ManyToOne`）
    - `applicant_user_id` -> `t_user(user_id)`
    - `approved_by` -> `t_user(user_id)`
  - SQL 外键约束在 `devicedb_reset.sql` 的 CREATE TABLE 段落中声明（事实）。

---

3) 索引（Indexes）

说明：索引定义事实来自 `backend/db/devicedb_reset.sql`，并在 Repository 查询中被使用。

- 定义（事实，位于 `devicedb_reset.sql`）：
  ```sql
  CREATE INDEX idx_user_staff_id ON t_user(staff_id);
  CREATE INDEX idx_device_name ON t_device(device_name);
  CREATE INDEX idx_device_is_loaned ON t_device(is_loaned);
  CREATE INDEX idx_device_status ON t_device(status);
  CREATE INDEX idx_loan_device_id ON t_loan_record(device_id);
  CREATE INDEX idx_loan_applicant_id ON t_loan_record(applicant_user_id);
  CREATE INDEX idx_loan_approval_status ON t_loan_record(approval_status);
  CREATE INDEX idx_loan_apply_date ON t_loan_record(apply_date);
  ```
- 使用位置（代码）: 查询方法如 `DeviceRepository.findByFilters`、`LoanRecordRepository.findByFilters` 会触发这些索引的使用（JPQL / Criteria / native queries 均可受益）。

---

4) 视图（Views）

说明：项目中用于统计的视图由 SQL 初始化脚本定义，后端可直接查询或存储过程引用。

- 已实现视图（`devicedb_reset.sql`）：
  ```sql
  CREATE OR REPLACE VIEW vw_device_status_distribution AS
  SELECT status AS device_status, COUNT(*) AS cnt
  FROM t_device
  GROUP BY status;
  ```
- 使用位置（代码）：`DashboardService` 可直接查询该视图或调用存储过程来获取相同聚合结果。

---

5) 存储过程（Stored Procedures）

说明：初始化脚本中包含用于初始化/刷新聚合表与直接按状态分布查询的存储过程，后端通过 `SimpleJdbcCall` 或 `JdbcTemplate` 调用这些过程。

- 定义位置：`backend/db/devicedb_reset.sql`（事实）
- 已实现存储过程示例（在脚本中）：
  ```sql
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
  DELIMITER ;
  
  -- 状态分布过程
  DELIMITER $$
  CREATE PROCEDURE sp_get_status_distribution()
  BEGIN
    SELECT status AS device_status, COUNT(*) AS cnt FROM t_device GROUP BY status;
  END$$
  DELIMITER ;
  ```
- 调用位置：`backend/src/main/java/com/dms/service/DashboardService.java` 使用 `SimpleJdbcCall`/`JdbcTemplate` 并在 Controller 暴露为 `/api/dashboard/status-distribution`。

---

6) 触发器（Triggers）

说明：触发器在 SQL 初始化脚本中实现，用于对 `dashboard_aggregates` 单行汇总表进行插入/更新/删除时的增量维护。

- 定义位置：`backend/db/devicedb_reset.sql`
- 触发器示例（事实）：
  ```sql
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
  END;
  ```
- 同理脚本中包含 `AFTER UPDATE` 与 `AFTER DELETE` 的实现（事实）。

---

7) 备份（Backup）

说明：项目实现了一个备份服务，运行时会在 `backend/db/` 下写入 mysqldump 导出的 SQL 文件;

- 实现文件（事实）：`backend/src/main/java/com/dms/service/DatabaseBackupService.java`
- 运行行为（事实）：
  - 启动时检查当天备份文件是否存在（`backup-YYYYMMDD.sql`），不存在则尝试使用 `mysqldump` 生成初始备份（`runBackup` 方法）。
  - 定时任务 `@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Shanghai")` 每日执行备份（如 `scheduledDailyBackup`）。
- 备份导出示例命令（用于实验报告）：
  ```powershell
  mysqldump -u root -proot --single-transaction --routines --triggers --databases devicedb > backend/db/backup-20251223.sql
  ```
- 恢复（导入）示例命令：
  ```powershell
  mysql -u root -proot < backend/db/backup-20251223.sql
  ```

---

8) 并发（Locks / 并发控制）

说明：项目同时使用乐观锁（JPA `@Version`）和在关键事务中使用的悲观锁（Repository + FOR UPDATE 模式）。下列为事实实现位置与等价 SQL 示例。

A. 乐观锁（实体层）
- 实现位置（事实）：
  - `backend/src/main/java/com/dms/entity/Device.java`（`@Version` 字段 `version`）
  - `backend/src/main/java/com/dms/entity/LoanRecord.java`（`@Version` 字段 `version`）
- 等价 SQL 示范（用于实验报告演示）：
  ```sql
  -- 乐观锁检查（应用层需要检查受影响行数）
  UPDATE t_device SET device_name = ?, version = version + 1 WHERE device_id = ? AND version = ?;
  ```

B. 悲观锁（Repository / Service）
- 实现位置（事实）：
  - Repository: `backend/src/main/java/com/dms/repository/DeviceRepository.java`（`findByIdWithLock` 使用 `@Lock(LockModeType.PESSIMISTIC_WRITE)`）
  - Service: `backend/src/main/java/com/dms/service/LoanRecordService.java` 在 `apply`/`approve`/`returnDevice` 中调用该方法以获得行级锁。
- 等价 SQL（实际效果）：
  ```sql
  START TRANSACTION;
  SELECT device_id, is_loaned, version FROM t_device WHERE device_id = ? FOR UPDATE;
  -- 若可用则更新：
  UPDATE t_device SET is_loaned = TRUE, current_holder_id = ?, loan_date = CURRENT_DATE, version = version + 1 WHERE device_id = ?;
  COMMIT;
  ```

C. 事务示例（综合借用批准流程，事实对应 `LoanRecordService.approve`）
- Java 层在事务内进行：查询借用记录 -> 验证 -> 获得设备悲观锁 -> 更新设备与借用记录 -> 保存。SQL 等价片段见上文 CRUD / 借用记录部分。

---

附录：关键文件快速索引（事实定位）
- SQL 初始化脚本： `backend/db/devicedb_reset.sql`（包含建表、索引、视图、存储过程、触发器、种子数据）
- 实体类：
  - `backend/src/main/java/com/dms/entity/User.java`
  - `backend/src/main/java/com/dms/entity/Device.java`
  - `backend/src/main/java/com/dms/entity/LoanRecord.java`
- Controller / Service / Repository（示例）：
  - User: `controller/UserController.java`, `service/UserService.java`, `repository/UserRepository.java`
  - Device: `controller/DeviceController.java`, `service/DeviceService.java`, `repository/DeviceRepository.java`
  - LoanRecord: `controller/LoanRecordController.java`, `service/LoanRecordService.java`, `repository/LoanRecordRepository.java`
- Dashboard 存储过程调用： `service/DashboardService.java`, `controller/DashboardController.java`
- 备份服务： `service/DatabaseBackupService.java`

---
