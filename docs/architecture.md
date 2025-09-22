# 系统架构概览

本文件描述畅购严选电商系统的整体架构与模块关系。

## 1. 总体视图
```
Browser / App
   │
   ▼
OpenResty (静态资源与接口灰度控制)
   │
   ▼
Spring Cloud Gateway (南北向 / 东西向网关)
   ├── yx-auth (账号服务)
   └── yx-store (商家服务)
```

- **OpenResty**：根据请求域名、Cookie、Header 决定访问灰度还是生产目录，并将接口请求转发至 Spring Cloud Gateway。
- **Spring Cloud Gateway**：统一负载与安全入口，根据 `X-Gray`、`X-User-Id`、流量百分比等信息选择灰度或生产实例。
- **yx-auth**：负责主账号体系，提供 JWT 登录、角色权限、验证码、自动续期等能力。
- **yx-store**：提供商家与商品能力，并对灰度环境做数据隔离。

## 2. Starter 体系
```
yx-common
 ├── yx-common-core-starter
 │     ├─ 全局异常处理
 │     ├─ 统一响应模型
 │     └─ ObjectMapper / Clock 配置
 ├── yx-common-log-starter
 │     └─ TraceId 过滤器 + MDC 链路追踪
 └── yx-common-mybatis-starter
       └─ Mapper 扫描、数据源整合
```

各业务服务只需引入对应 starter，即可获得统一的序列化、日志、数据库能力。

## 3. 灰度链路
1. 客户端访问统一域名。
2. OpenResty 根据 Cookie/Header 将静态资源与接口转发到灰度或生产目录。
3. Spring Cloud Gateway 读取 `X-Gray` / `X-User-Id` / `SERVICE-TAG`，通过自定义 LoadBalancer 过滤 Nacos 实例。
4. yx-auth / yx-store 通过 `SERVICE_VERSION` 环境变量向 Nacos 注册 `version=gray|prd` 元数据，确保链路闭环。

## 4. 开发建议
- 若要接入数据库，可在 `yx-common-mybatis-starter` 中扩展数据源配置。
- 若要接入消息或缓存，可新增 `yx-common-redis-starter` 等模块。
- 前端可通过读取 `/api/auth/me` 获取菜单与资源，动态渲染界面。
