# 智维设备管理系统 (Device Management System)

一个基于 Vue.js 前端与 Spring Boot 后端的设备管理完整示例项目。

## 项目结构（简要）

```
Device_Management_System/
├── src/                    # Vue.js frontend source
│   ├── api/               # API service layer (Axios)
│   ├── components/        # Vue components
│   ├── router/            # Vue Router configuration
│   └── main.js            # Application entry
├── backend/               # Spring Boot backend
│   ├── src/main/java/     # Java source code
│   └── src/main/resources/ # Configuration & migrations
└── public/                # Static assets
```

## 主要功能

- 仪表盘：关键统计数据与图表（设备总数、待处理申请等）
- 设备管理：设备增删改查、分页与搜索
- 用户管理：用户信息维护与权限分配
- 借用管理：设备借用申请、审批、归还记录与逾期统计
- 身份认证：基于 JWT 的登录与权限控制（管理员/普通用户）
- 数据库迁移：使用 Flyway 管理初始表结构与种子数据
- 可选：Redis 缓存支持（若在配置中启用）

## 快速启动

前提：已安装 Node.js、npm、JDK 与 Maven

1) 前端

```bash
# Install dependencies
npm install

# Development server
npm run serve

# Production build
npm run build

# Lint
npm run lint
```

前端默认通过环境变量 `VUE_APP_API_URL` 指向后端 API，例如：

  VUE_APP_API_URL=http://localhost:8081/api

2) 后端

```bash
# Navigate to backend
cd backend

# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run
```

## 数据库与迁移

- 项目使用 Flyway 管理数据库迁移脚本，文件位于 `backend/src/main/resources/db/migration/`。
- 开发时可使用内嵌 H2 或本地 MySQL。生产环境建议使用 MySQL 并配置连接信息。
- 数据库连接配置在 `backend/src/main/resources/application.properties`。请根据实际数据库修改以下常用属性：

  spring.datasource.url=jdbc:mysql://localhost:3306/dms?useSSL=false&serverTimezone=UTC
  spring.datasource.username=your_db_user
  spring.datasource.password=your_db_password

- 如果启用了 Redis，请在 `application.properties` 中配置 Redis 主机与端口；否则相关代码会退化为非缓存模式。

## 常用 API 概览（示例）
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/login | User login |
| POST | /api/auth/register | User registration |
| GET | /api/devices | List devices |
| POST | /api/devices | Create device |
| PUT | /api/devices/{id} | Update device |
| DELETE | /api/devices/{id} | Delete device (Admin) |
| GET | /api/users | List users |
| POST | /api/loans | Apply for loan |
| PUT | /api/loans/{id}/approve | Approve loan (Admin) |
| PUT | /api/loans/{id}/reject | Reject loan (Admin) |
| POST | /api/loans/{id}/return | Return device |


具体接口与请求/响应格式请参考源码中 `controller` 包下的实现或前端 `api` 目录中对接的调用。

## 默认账号（开发用）

| Role | Staff ID | Password |
|------|----------|----------|
| Admin | ADMIN001 | admin123 |
| User | E001 | admin123 |


## 贡献与许可证

WTFPL ,感谢GPT-5-mini对本项目大力支持
