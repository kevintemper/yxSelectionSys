# 严选电商系统 (Yanxuan E-Commerce System)

## 📖 项目简介
严选系统是一个基于 **SpringCloud Alibaba** 的电商后台管理平台，采用 **JDK 17** 开发，前端使用 **React + Ant Design + Vite**。  
本系统目标是支持 **商家管理、会员管理、运营管理、数据看板** 等功能，同时实现 **灰度发布链路**，方便在实际场景中进行灰度测试和生产环境切换。

---

## 📂 项目模块
- **yx-auth**  
  - 提供账号注册、登录、JWT 鉴权、验证码校验、记住登录等功能
- **yx-store**  
  - 商家管理服务，支持商家入驻、商品管理、外部合作等
- **gateway**  
  - Spring Cloud Gateway + OpenResty 网关，支持服务路由与灰度流量控制
- **前端 (yx-admin)**  
  - React + Ant Design 管理后台，提供菜单路由和界面展示

---

## ✅ 已实现功能
- [x] 项目初始化与多模块管理（Maven + Spring Boot Starter）
- [x] yx-auth 服务：账号体系 + JWT 登录（7天记住登录，自动续期）
- [x] 验证码接口（前后端联调可用）
- [x] yx-store 服务：基础商家服务搭建
- [x] Nacos 注册中心集成，支持多环境 (`gray` / `prd`) 标签
- [x] 基础前端路由跳转与管理后台框架
- [x] 灰度链路支持：  
  - 按用户 ID、流量百分比、接口维度进行灰度控制  
  - 灰度服务只能调用灰度链路，生产服务只能调用生产链路

---

## 🚧 未完成功能
以下功能在文档中提到，但尚未实现或只完成部分：
- [ ] 商家管理模块（账号、角色、菜单、资源权限）
- [ ] 会员管理模块（会员注册与管理）
- [ ] 运营账号体系（优惠券、活动规则）
- [ ] 数据看板（商家 / 会员 / 运营可视化）
- [ ] OpenResty Lua 灰度脚本完整接入
- [ ] 前端界面美化与主题优化（目前是基础风格）

---

## 🛠 本地运行

### 后端
1. 启动 Nacos (默认端口 `8848`)  
2. 分别运行以下服务：
   ```bash
   cd yx-auth
   mvn spring-boot:run
   
   cd yx-store
   mvn spring-boot:run
   
   cd gateway
   mvn spring-boot:run
   ```

### 前端
```bash
cd yx-admin
npm install
npm run dev
```

访问地址: `http://localhost:5173`

---

## 📌 技术栈
- **后端**: Spring Boot, SpringCloud Alibaba, Nacos, Gateway, OpenResty  
- **数据库**: MySQL  
- **前端**: React, Ant Design, Vite  
- **工具链**: Maven, Git, Docker (可选)

---

## 🗂 项目目录
```
project/
├── yx-auth         # 账号服务
├── yx-store        # 商家服务
├── gateway         # 网关
├── yx-admin        # 前端项目
├── pom.xml         # 父项目配置
└── README.md
```


