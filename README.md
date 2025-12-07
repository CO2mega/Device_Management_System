# 智维设备管理系统 (Device Management System)

一个基于 Vue.js 前端与 Spring Boot 后端的设备管理完整示例项目。

## 项目结构（简要）

- backend/              # Spring Boot 后端代码
  - src/main/java       # Java 源码
  - src/main/resources  # 配置与数据库迁移脚本（Flyway）
- src/                  # 前端（Vue.js）源代码
  - api/                # 与后端交互的 Axios 封装
  - components/         # Vue 组件
  - router/             # 路由配置
  - main.js             # 前端入口
- public/               # 静态资源
- README.md             # 本文件

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

- 进入项目根目录
- 安装依赖并启动开发服务器：

  npm install
  npm run serve

- 打包发行：

  npm run build

前端默认通过环境变量 `VUE_APP_API_URL` 指向后端 API，例如：

  VUE_APP_API_URL=http://localhost:8081/api

2) 后端

- 进入 `backend` 目录：

  cd backend

- 构建并运行（跳过测试以加速）：

  mvn clean package -DskipTests
  mvn spring-boot:run

- 默认后端运行端口为 8081（可在 `backend/src/main/resources/application.properties` 中修改）

## 数据库与迁移

- 项目使用 Flyway 管理数据库迁移脚本，文件位于 `backend/src/main/resources/db/migration/`。
- 开发时可使用内嵌 H2 或本地 MySQL。生产环境建议使用 MySQL 并配置连接信息。
- 数据库连接配置在 `backend/src/main/resources/application.properties`。请根据实际数据库修改以下常用属性：

  spring.datasource.url=jdbc:mysql://localhost:3306/dms?useSSL=false&serverTimezone=UTC
  spring.datasource.username=your_db_user
  spring.datasource.password=your_db_password

- 如果启用了 Redis，请在 `application.properties` 中配置 Redis 主机与端口；否则相关代码会退化为非缓存模式。

备注：启动时是否每次都跑三条 SQL 取决于 Flyway 配置（默认 Flyway 会在表未存在时执行迁移，若设置为 clean 或可重复迁移可能会重置数据）。请在开发/测试与生产环境中分别确认 Flyway 策略。

## 常见问题排查

- 如果发生 403（Forbidden）错误，请检查请求是否带上正确的 JWT Token，以及后端安全配置（是否允许匿名访问某些接口）。
- 若出现编译错误（例如与 JJWT 版本不兼容），请检查 `backend/pom.xml` 中的依赖版本，或调整对应代码以兼容当前依赖。
- Redis 不是必须的，但若功能依赖 Redis（例如会话或缓存），需在系统中安装并在 `application.properties` 中配置连接地址；否则可以禁用相关配置。

## 常用 API 概览（示例）

- POST /api/auth/login — 用户登录（返回 JWT）
- POST /api/auth/register — 用户注册
- GET  /api/devices — 列出设备（支持分页/搜索）
- POST /api/devices — 新增设备（需权限）
- PUT  /api/devices/{id} — 更新设备
- DELETE /api/devices/{id} — 删除设备（管理员）
- GET  /api/users — 列出用户
- POST /api/loans — 创建借用申请
- PUT  /api/loans/{id}/approve — 批准借用（管理员）
- PUT  /api/loans/{id}/reject — 驳回借用（管理员）
- POST /api/loans/{id}/return — 归还设备

具体接口与请求/响应格式请参考源码中 `controller` 包下的实现或前端 `api` 目录中对接的调用。

## 默认账号（开发用）

- 管理员：Staff ID = ADMIN001，密码 = admin123
- 普通用户：Staff ID = E001，密码 = admin123

（请在生产环境中修改默认账号与密码）

## 贡献与许可证

本项目为教学/示例用途，代码采用宽松许可，可根据需要调整并用于学习或扩展。

---
更新日期：2025-12-08
