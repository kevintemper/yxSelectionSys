# 畅购严选电商系统

一个基于 **Spring Boot 3 + Spring Cloud Alibaba 2023**、全面支持 **JDK 17** 的现代化电商后端解决方案。项目围绕“严选”场景构建，聚焦账号体系、商家服务、统一网关及灰度发布链路，提供可复用的企业级脚手架。

## 🔧 架构概览
```
                ┌─────────────────────────────────────────┐
                │                 OpenResty               │
                │   - 静态资源目录灰度治理                │
用户 (PC/APP) ──┤   - Lua 脚本转发灰度/生产后端          ├─▶ Spring Cloud Gateway ──▶ yx-auth
                │                                         │                         └─▶ yx-store
                └─────────────────────────────────────────┘
```
* **南北向网关**：OpenResty + Spring Cloud Gateway 负责前端/外部调用，按 Header / Cookie / 用户 ID 灰度。
* **东西向网关**：Gateway 额外暴露 `kaiwen-proxy` 前缀，供集团内部系统调用，并携带目标环境标识。
* **微服务注册**：Nacos 以 `version=gray|prd` 元数据标记环境，Gateway 根据请求上下文选择目标实例。

## 📦 模块说明
| 模块 | 描述 |
| ---- | ---- |
| `yx-common` | 公共 Starter 集合。提供 JSON 规范、异常拦截、MDC Trace、MyBatis 快速集成等能力。|
| `yx-auth` | 主账号服务，支持商家账号、角色、菜单资源定义，JWT 登录、验证码校验、自动续期。|
| `yx-store` | 商家域服务，提供商家/商品基础能力，并支持按灰度标签过滤产品。|
| `yx-gateway` | Spring Cloud Gateway 网关，内置灰度路由、南北/东西向过滤器、环境隔离。|
| `openresty` | 网关层 Lua 脚本与示例配置，控制静态资源目录与接口转发一致灰度。|
| `yx-admin` | Vite + React 管理前端。内置灰度环境切换、仪表盘、商家管理导航及深色/浅色主题。|
| `docs` | 架构设计与灰度流量控制方案说明。|

## ✅ 关键能力
- **统一 Starter**
  - `yx-common-core-starter`：全局响应包装、异常处理、基础 Bean 配置。
  - `yx-common-log-starter`：MDC TraceId 过滤器，支持多线程日志追踪。
  - `yx-common-mybatis-starter`：约定式 Mapper 扫描、Hikari 数据源整合。
- **主账号体系 (yx-auth)**
  - 商家账号/角色/菜单/资源模型，提供 CRUD 接口。
  - JWT 鉴权 + 7 天记住登录 + 临期自动续期。
  - 图形验证码接口，支持登录安全管控。
  - OpenAPI 文档自动生成，便于前后端联调。
- **商家域服务 (yx-store)**
  - 商家/商品内存实现，展示服务编排与灰度过滤策略。
  - 根据 `X-Gray` 请求头过滤灰度商品，实现链路一致性。
- **网关与灰度**
  - Header / Cookie / 用户 ID / 接口前缀多维灰度控制。
  - Gateway 动态感知 `version` 元数据，灰度实例只调灰度实例。
  - 南北向 (外部) 与东西向 (内部) 流量隔离示例。
- **前端管理台 (yx-admin)**
  - 登录页支持图形验证码、记住登录态，并在会话即将过期时静默刷新 Token。
  - 管理后台提供菜单树渲染、主题切换、全局搜索框以及灰度/生产环境切换器。
  - Axios 拦截器自动携带 `X-Env`/`X-Gray`/`SERVICE-TAG`，确保前后端链路一致。

## 🚀 快速开始
1. 启动 [Nacos](https://nacos.io/) 并创建 `gray`、`prd` 命名空间（或使用默认 `public`）。
2. 为 `yx-auth`、`yx-store` 分别运行两个实例，设置 `SERVICE_VERSION=gray` 或 `prd` 环境变量注册到不同环境。
3. 启动 Gateway：
   ```bash
   mvn -pl yx-gateway spring-boot:run
   ```
4. 启动 Auth 与 Store 服务：
   ```bash
   mvn -pl yx-auth spring-boot:run
   mvn -pl yx-store spring-boot:run
   ```
5. 启动管理前端：
   ```bash
   cd yx-admin
   npm install
   npm run dev
   ```
   - 开发环境默认代理到 `http://localhost:9101` (灰度) 与 `http://localhost:9103` (生产)，可在 `vite.config.ts` 调整。
   - 登录后可在右上角切换灰度/生产环境，浏览器会写入 `env=gray|prd` Cookie 与 LocalStorage，OpenResty 与 Gateway 会读取该标记。
6. 启动 OpenResty：
   ```bash
   cd openresty
   npm install  # 安装 lua-resty-auto-ssl 等依赖（如需）
   npm run start # 或 docker-compose up，自行选择部署方式
   ```
   - `conf/nginx.conf` 演示南北向、东西向流量分发与静态资源灰度目录（`sites/prd`、`sites/gray`）。
   - 更新 `upstream` 指向你本地启动的 Gateway/Service 端口即可。
7. 登录示例：
   - 获取验证码：`GET /api/auth/captcha`
   - 登录接口：`POST /api/auth/login`
   - 默认管理员账号：`admin / ChangeMe123!`

## 🧪 灰度体验
- 设置请求头 `X-Gray: gray` 或 `X-User-Id: 123gray`，Gateway 自动转发至灰度实例。
- OpenResty `conf` 目录示例将静态文件与接口请求拆分目录，实现前后端一体化灰度。
- `X-Gray` 透传至 `yx-store`，仅返回灰度商品，确保数据链路一致。

## 🧱 目录结构
```
.
├── docs/                      # 架构与灰度说明
├── openresty/                 # Lua 网关脚本
├── pom.xml                    # 顶层聚合工程
├── yx-common/                 # 公共 Starter 聚合模块
│   ├── yx-common-core-starter
│   ├── yx-common-log-starter
│   └── yx-common-mybatis-starter
├── yx-auth/                   # 账号服务
├── yx-store/                  # 商家服务
└── yx-gateway/                # 网关服务
```

## 📚 设计文档
- `docs/architecture.md`：整体架构与服务关系
- `docs/gray-strategy.md`：灰度策略与 OpenResty 脚本示例

> 项目仍在持续建设中，欢迎根据业务需要扩展数据库落地、前端 UI 以及更多业务模块。
