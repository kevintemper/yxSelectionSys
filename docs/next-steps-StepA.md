# 下一步实施计划（Step A）
1. **仓库骨架**：建立 Maven 多模块工程：`yx-parent`（父 POM）、`yx-common`（通用包：异常、响应体、常量、JWT 工具）、`yx-starter`（统一 Starter）、`yx-gateway`、`yx-auth`、`yx-store`。
2. **基础配置**：提供 `application.yml` 与 `bootstrap.yml` 模板，约定 `gray` / `prd` profile；预置 Nacos 命名空间与服务分组占位。
3. **鉴权链路**：在 `yx-auth` 中放置 JWT 登录/刷新接口骨架，包含“记住 7 天”与续期的占位逻辑与单元测试样例。
4. **菜单与权限**：定义菜单/角色/资源的基础表结构（DDL 草案）与 MyBatis（或 JPA）实体；提供最小可运行的 API 占位（CRUD）。
5. **网关与灰度**：在 `yx-gateway` 中预置基于请求头/用户ID/百分比的路由占位策略；同时输出 OpenResty 与 Lua 灰度规则样板文件（占位）。
6. **前端静态部署位**：准备多环境目录结构（`gray/`、`prd/`），并在 README 中写明 OpenResty 目录映射示例。
7. **本地一键编排**：准备 `docker-compose.yml` 占位（Nacos + 网关 + 两套服务示例），确保开发者可一键拉起最小链路。
8. **验收清单**：输出 CheckList（登录态 7 天、自动续期、gray→gray 调用约束、菜单树读写、基础 CRUD、网关转发校验）。

> 本次提交优先落地 1~3 与 8 的骨架与文档，不破坏你现有代码；其余逐步补全。